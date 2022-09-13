package uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus;

import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;
import com.azure.messaging.servicebus.models.DeadLetterOptions;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.bulkscan.payment.processor.client.processor.ProcessorClient;
import uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.exceptions.InvalidMessageException;
import uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.exceptions.UnknownMessageProcessingResultException;
import uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.handler.MessageProcessingResult;
import uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.handler.MessageProcessingResultType;
import uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.handler.PaymentMessageHandler;
import uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.model.CreatePaymentMessage;
import uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.model.UpdatePaymentMessage;

import static uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.handler.MessageProcessingResultType.POTENTIALLY_RECOVERABLE_FAILURE;
import static uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.handler.MessageProcessingResultType.SUCCESS;
import static uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.handler.MessageProcessingResultType.UNRECOVERABLE_FAILURE;


@Service
@Profile("!functional & !integration")
public class PaymentMessageProcessor {

    private static final Logger log = LoggerFactory.getLogger(PaymentMessageProcessor.class);

    private final PaymentMessageHandler paymentMessageHandler;
    private final PaymentMessageParser paymentMessageParser;
    private final ProcessorClient processorClient;
    private final int maxDeliveryCount;

    public PaymentMessageProcessor(
        PaymentMessageHandler paymentMessageHandler,
        PaymentMessageParser paymentMessageParser,
        ProcessorClient processorClient,
        @Value("${azure.servicebus.payments.max-delivery-count}") int maxDeliveryCount
    ) {
        this.paymentMessageHandler = paymentMessageHandler;
        this.paymentMessageParser = paymentMessageParser;
        this.processorClient = processorClient;
        this.maxDeliveryCount = maxDeliveryCount;
    }

    /**
     * Reads and processes next message from the queue.
     * return false if there was no message to process. Otherwise true.
     */
    public void processNextMessage(ServiceBusReceivedMessageContext serviceBusReceivedMessageContext) {
        ServiceBusReceivedMessage message = serviceBusReceivedMessageContext.getMessage();
        if (message != null) {
            if (message.getSubject() == null) {
                deadLetterTheMessage(serviceBusReceivedMessageContext, "Missing label", null);
            } else {
                switch (message.getSubject()) {
                    case "CREATE" -> {
                        MessageProcessingResult result = processCreateCommand(message);
                        tryFinaliseProcessedMessage(serviceBusReceivedMessageContext, result);
                    }
                    case "UPDATE" -> {
                        var updateResult = processUpdateCommand(message);
                        tryFinaliseProcessedMessage(serviceBusReceivedMessageContext, updateResult);
                    }
                    default -> deadLetterTheMessage(
                        serviceBusReceivedMessageContext,
                        "Unrecognised message type: " + message.getSubject(),
                        null
                    );
                }
            }
        } else {
            log.info("""
                         No payment messages to process by payment processor!!
                         """);
        }

    }

    private MessageProcessingResult processCreateCommand(ServiceBusReceivedMessage message) {
        log.info("Started processing payment message with ID {}", message.getMessageId());

        CreatePaymentMessage payment = null;

        try {
            payment = paymentMessageParser.parse(message.getBody());
            paymentMessageHandler.handlePaymentMessage(payment, message.getMessageId());
            processorClient.updatePayments(payment.payments);
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

    private MessageProcessingResult processUpdateCommand(ServiceBusReceivedMessage message) {
        log.info("Started processing update payment message with ID {}", message.getMessageId());

        UpdatePaymentMessage payment = null;

        try {
            payment = paymentMessageParser.parseUpdateMessage(message.getBody());
            paymentMessageHandler.updatePaymentCaseReference(payment);
            log.info(
                "Processed update payment message with ID {}. Envelope ID: {}",
                message.getMessageId(),
                payment.envelopeId
            );
            return new MessageProcessingResult(SUCCESS);
        } catch (InvalidMessageException ex) {
            log.error(
                "Rejected update payment message with ID {}, because it's invalid",
                message.getMessageId(),
                ex
            );
            return new MessageProcessingResult(UNRECOVERABLE_FAILURE, ex);
        } catch (Exception ex) {
            logUpdateMessageProcessingError(message, payment, ex);
            return new MessageProcessingResult(POTENTIALLY_RECOVERABLE_FAILURE);
        }
    }

    private void tryFinaliseProcessedMessage(
        ServiceBusReceivedMessageContext messageContext,
        MessageProcessingResult processingResult
    ) {
        try {
            finaliseProcessedMessage(messageContext, processingResult);
        } catch (Exception ex) {
            logMessageFinaliseError(messageContext, processingResult.resultType, ex);
        }
    }

    private void finaliseProcessedMessage(
        ServiceBusReceivedMessageContext messageContext,
        MessageProcessingResult processingResult
    ) {
        var message = messageContext.getMessage();
        switch (processingResult.resultType) {
            case SUCCESS:
                messageContext.complete();
                log.info("Payment Message with ID {} has been completed", message.getMessageId());
                break;
            case UNRECOVERABLE_FAILURE:
                deadLetterTheMessage(
                    messageContext,
                    "Payment Message processing error",
                    processingResult.exception.getMessage()
                );
                break;
            case POTENTIALLY_RECOVERABLE_FAILURE:
                deadLetterIfMaxDeliveryCountIsReached(messageContext);
                break;
            default:
                throw new UnknownMessageProcessingResultException(
                    "Unknown payment message processing result type: " + processingResult.resultType
                );
        }
    }

    private void deadLetterIfMaxDeliveryCountIsReached(ServiceBusReceivedMessageContext messageContext) {

        var message = messageContext.getMessage();
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
                messageContext,
                "Too many deliveries",
                "Reached limit of message delivery count of " + deliveryCount
            );
        }
    }

    private void deadLetterTheMessage(
        ServiceBusReceivedMessageContext messageContext,
        String reason,
        String description
    ) {
        var message = messageContext.getMessage();
        messageContext.deadLetter(
            new DeadLetterOptions().setDeadLetterReason(reason).setDeadLetterErrorDescription(description)
        );

        log.error(
            "Payment Message with ID {} has been dead-lettered. Reason: '{}'. Description: '{}'",
            message.getMessageId(),
            reason,
            description
        );
    }

    private void logMessageFinaliseError(
        ServiceBusReceivedMessageContext messageContext,
        MessageProcessingResultType processingResultType,
        Exception ex
    ) {
        log.error(
            "Failed to process payment message with ID {}. Processing result: {}",
            messageContext.getMessage().getMessageId(),
            processingResultType,
            ex
        );
    }

    private void logMessageProcessingError(
        ServiceBusReceivedMessage message,
        CreatePaymentMessage paymentMessage,
        Exception exception
    ) {
        String baseMessage = String.format("Failed to process payment message with ID %s", message.getMessageId());
        String fullMessage = paymentMessage != null
            ? String.format(
                "%s. CCD Case Number: %s, Jurisdiction: %s",
                baseMessage,
                paymentMessage.ccdReference,
                paymentMessage.jurisdiction
            )
            : baseMessage;
        String fullMessageWithClientResponse = exception instanceof FeignException feignException
            ? String.format("%s. Client response: %s", fullMessage, ((FeignException) exception).contentUTF8())
            : fullMessage;

        log.error(fullMessageWithClientResponse, exception);
    }

    private void logUpdateMessageProcessingError(
        ServiceBusReceivedMessage message,
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
        String fullMessageWithClientResponse = exception instanceof FeignException feignException
            ? String.format("%s. Client response: %s", fullMessage, ((FeignException) exception).contentUTF8())
            : fullMessage;

        log.error(fullMessageWithClientResponse, exception);
    }
}
