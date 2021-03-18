package uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus;

import com.microsoft.azure.servicebus.IMessage;
import com.microsoft.azure.servicebus.IMessageReceiver;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.bulkscan.payment.processor.logging.AppInsights;
import uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.exceptions.InvalidMessageException;
import uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.exceptions.UnknownMessageProcessingResultException;
import uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.handler.MessageProcessingResult;
import uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.handler.MessageProcessingResultType;
import uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.handler.PaymentMessageHandler;
import uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.model.CreatePaymentMessage;
import uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.model.UpdatePaymentMessage;

import java.util.function.Consumer;

import static uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.handler.MessageProcessingResultType.POTENTIALLY_RECOVERABLE_FAILURE;
import static uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.handler.MessageProcessingResultType.SUCCESS;
import static uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.handler.MessageProcessingResultType.UNRECOVERABLE_FAILURE;


@Service
@Profile("!functional & !integration")
public class PaymentMessageProcessor {

    public static final String NOT_AVAILABLE = "Not available";
    public static final String LETTER_REASON_PROCESSING_ERROR = "Payment Message processing error";
    private static final Logger log = LoggerFactory.getLogger(PaymentMessageProcessor.class);

    private final PaymentMessageHandler paymentMessageHandler;
    private final IMessageReceiver messageReceiver;
    private final PaymentMessageParser paymentMessageParser;
    private final int maxDeliveryCount;
    private final AppInsights appInsights;

    public PaymentMessageProcessor(
        PaymentMessageHandler paymentMessageHandler,
        IMessageReceiver messageReceiver,
        PaymentMessageParser paymentMessageParser,
        @Value("${azure.servicebus.payments.max-delivery-count}") int maxDeliveryCount,
        AppInsights appInsights
    ) {
        this.paymentMessageHandler = paymentMessageHandler;
        this.messageReceiver = messageReceiver;
        this.paymentMessageParser = paymentMessageParser;
        this.maxDeliveryCount = maxDeliveryCount;
        this.appInsights = appInsights;
    }

    /**
     * Reads and processes next message from the queue.
     *
     * @return false if there was no message to process. Otherwise true.
     */
    public boolean processNextMessage() throws ServiceBusException, InterruptedException {
        IMessage message = messageReceiver.receive();
        if (message != null) {
            if (message.getLabel() == null) {
                deadLetterTheMessage(message, "Missing label", null);
            } else {
                switch (message.getLabel()) {
                    case "CREATE":
                        MessageProcessingResult result = processCreateCommand(message);
                        tryFinaliseProcessedMessage(message, result);
                        break;
                    case "UPDATE":
                        var updateResult = processUpdateCommand(message);
                        tryFinaliseProcessedMessage(message, updateResult);
                        break;
                    default:
                        deadLetterTheMessage(message, "Unrecognised message type: " + message.getLabel(), null);
                }
            }
        } else {
            log.info("No payment messages to process by payment processor!!");
        }

        return message != null;
    }

    private MessageProcessingResult processCreateCommand(IMessage message) {
        log.info("Started processing payment message with ID {}", message.getMessageId());

        CreatePaymentMessage payment = null;

        try {
            payment = paymentMessageParser.parse(message.getMessageBody());
            paymentMessageHandler.handlePaymentMessage(payment, message.getMessageId());

            log.info(
                "Processed payment message with ID {}. Envelope ID: {}",
                message.getMessageId(),
                payment.envelopeId
            );

            return new MessageProcessingResult(SUCCESS);
        } catch (InvalidMessageException ex) {
            log.error("Rejected payment message with ID {}, because it's invalid", message.getMessageId(), ex);
            return new MessageProcessingResult(UNRECOVERABLE_FAILURE, ex);
        } catch (Exception ex) {
            logMessageProcessingError(message, payment, ex);
            return new MessageProcessingResult(POTENTIALLY_RECOVERABLE_FAILURE);
        }
    }

    private MessageProcessingResult processUpdateCommand(IMessage message) {
        log.info("Started processing update payment message with ID {}", message.getMessageId());

        UpdatePaymentMessage payment = null;

        try {
            payment = paymentMessageParser.parseUpdateMessage(message.getMessageBody());
            paymentMessageHandler.updatePaymentCaseReference(payment);
            log.info(
                "Processed update payment message with ID {}. Envelope ID: {}",
                message.getMessageId(),
                payment.envelopeId
            );
            return new MessageProcessingResult(SUCCESS);
        } catch (InvalidMessageException ex) {
            log.error("Rejected update payment message with ID {}, because it's invalid", message.getMessageId(), ex);
            return new MessageProcessingResult(UNRECOVERABLE_FAILURE, ex);
        } catch (Exception ex) {
            logUpdateMessageProcessingError(message, payment, ex);
            return new MessageProcessingResult(POTENTIALLY_RECOVERABLE_FAILURE);
        }
    }

    private void tryFinaliseProcessedMessage(IMessage message, MessageProcessingResult processingResult) {
        try {
            finaliseProcessedMessage(message, processingResult);
        } catch (InterruptedException ex) {
            logMessageFinaliseError(message, processingResult.resultType, ex);
            Thread.currentThread().interrupt();
        } catch (Exception ex) {
            logMessageFinaliseError(message, processingResult.resultType, ex);
        }
    }

    private void finaliseProcessedMessage(
        IMessage message,
        MessageProcessingResult processingResult
    ) throws InterruptedException, ServiceBusException {

        switch (processingResult.resultType) {
            case SUCCESS:
                messageReceiver.complete(message.getLockToken());
                log.info("Payment Message with ID {} has been completed", message.getMessageId());
                break;
            case UNRECOVERABLE_FAILURE:
                deadLetterTheMessage(
                    message,
                    "Payment Message processing error",
                    processingResult.exception.getMessage()
                );
                break;
            case POTENTIALLY_RECOVERABLE_FAILURE:
                deadLetterIfMaxDeliveryCountIsReached(message);
                break;
            default:
                throw new UnknownMessageProcessingResultException(
                    "Unknown payment message processing result type: " + processingResult.resultType
                );
        }
    }

    private void deadLetterIfMaxDeliveryCountIsReached(IMessage message)
        throws InterruptedException, ServiceBusException {

        int deliveryCount = (int) message.getDeliveryCount() + 1;

        if (deliveryCount < maxDeliveryCount) {
            // do nothing - let the message lock expire
            log.info(
                "Allowing payment message with ID {} to return to queue (delivery attempt {})",
                message.getMessageId(),
                deliveryCount
            );
        } else {
            deadLetterTheMessage(
                message,
                "Too many deliveries",
                "Reached limit of message delivery count of " + deliveryCount
            );
        }
    }

    private void deadLetterTheMessage(
        IMessage message,
        String reason,
        String description
    ) throws InterruptedException, ServiceBusException {
        messageReceiver.deadLetter(
            message.getLockToken(),
            reason,
            description
        );

        log.error(
            "Payment Message with ID {} has been dead-lettered. Reason: '{}'. Description: '{}'",
            message.getMessageId(),
            reason,
            description
        );
        logEvent(message, reason, description);
    }

    private void logEvent(IMessage message, String reason, String description) {

        if (message.getLabel() == null) {
            appInsights.tracePaymentFailure(message, reason, () -> NOT_AVAILABLE, () -> NOT_AVAILABLE,
                () -> NOT_AVAILABLE, () -> NOT_AVAILABLE,  () -> NOT_AVAILABLE);
        } else {
            switch (message.getLabel()) {
                case "CREATE":
                    executeEvent(message, (imessage) -> {
                        var createPaymentMessage = paymentMessageParser.parse(message.getMessageBody());
                        appInsights.tracePaymentFailure(message, reason, () -> description,
                            () -> createPaymentMessage.envelopeId, () -> createPaymentMessage.jurisdiction,
                            () -> NOT_AVAILABLE, () -> createPaymentMessage.ccdReference); });
                    break;
                case "UPDATE":
                    executeEvent(message, (imessage) -> {
                        var payment = paymentMessageParser.parseUpdateMessage(message.getMessageBody());
                        appInsights.tracePaymentFailure(message, reason, () -> description, () -> payment.envelopeId,
                            () -> payment.jurisdiction, () -> payment.exceptionRecordRef, () -> payment.newCaseRef); });
                    break;
                default:
                    appInsights.tracePaymentFailure(message, reason, () -> NOT_AVAILABLE, () -> NOT_AVAILABLE,
                        () -> NOT_AVAILABLE, () -> NOT_AVAILABLE,  () -> NOT_AVAILABLE);
            }
        }
    }

    private void executeEvent(IMessage message, Consumer<IMessage> consume) {
        try {
            consume.accept(message);
        } catch (Exception exception) {
            appInsights.tracePaymentFailure(message, LETTER_REASON_PROCESSING_ERROR, exception::getMessage,
                () -> NOT_AVAILABLE, () -> NOT_AVAILABLE, () -> NOT_AVAILABLE, () -> NOT_AVAILABLE);
        }
    }

    private void logMessageFinaliseError(
        IMessage message,
        MessageProcessingResultType processingResultType,
        Exception ex
    ) {
        log.error(
            "Failed to process payment message with ID {}. Processing result: {}",
            message.getMessageId(),
            processingResultType,
            ex
        );
    }

    private void logMessageProcessingError(IMessage message, CreatePaymentMessage paymentMessage, Exception exception) {
        String baseMessage = String.format("Failed to process payment message with ID %s", message.getMessageId());
        String fullMessage = paymentMessage != null
            ? String.format(
                "%s. CCD Case Number: %s, Jurisdiction: %s",
                baseMessage,
                paymentMessage.ccdReference,
                paymentMessage.jurisdiction
            )
            : baseMessage;
        String fullMessageWithClientResponse = exception instanceof FeignException
            ? String.format("%s. Client response: %s", fullMessage, ((FeignException) exception).contentUTF8())
            : fullMessage;

        log.error(fullMessageWithClientResponse, exception);
    }

    private void logUpdateMessageProcessingError(
        IMessage message,
        UpdatePaymentMessage paymentMessage,
        Exception exception
    ) {
        String baseMessage = String.format(
            "Failed to process update payment message with ID %s",
            message.getMessageId()
        );
        String fullMessage = paymentMessage != null
            ? String.format(
                "%s. New Case Number: %s, Exception Record Ref: %s, Jurisdiction: %s",
                baseMessage,
                paymentMessage.newCaseRef,
                paymentMessage.exceptionRecordRef,
                paymentMessage.jurisdiction
            )
            : baseMessage;
        String fullMessageWithClientResponse = exception instanceof FeignException
            ? String.format("%s. Client response: %s", fullMessage, ((FeignException) exception).contentUTF8())
            : fullMessage;

        log.error(fullMessageWithClientResponse, exception);
    }
}
