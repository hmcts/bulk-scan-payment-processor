package uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus;

import com.google.common.collect.ImmutableMap;
import com.microsoft.azure.servicebus.IMessage;
import com.microsoft.azure.servicebus.IMessageReceiver;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.bulkscan.payment.processor.client.payhub.PayHubClientException;
import uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.exceptions.InvalidMessageException;
import uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.exceptions.MessageProcessingException;
import uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.handler.MessageProcessingResult;
import uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.handler.MessageProcessingResultType;
import uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.handler.PaymentMessageHandler;
import uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.model.PaymentMessage;

import java.time.Instant;

import static uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.handler.MessageProcessingResultType.POTENTIALLY_RECOVERABLE_FAILURE;
import static uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.handler.MessageProcessingResultType.SUCCESS;
import static uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.handler.MessageProcessingResultType.UNRECOVERABLE_FAILURE;


@Service
@Profile("!nosb") // do not register for the nosb (test) profile
public class PaymentMessageProcessor {

    private static final Logger log = LoggerFactory.getLogger(PaymentMessageProcessor.class);

    private final PaymentMessageHandler paymentMessageHandler;
    private final IMessageReceiver messageReceiver;
    private final PaymentMessageParser paymentMessageParser;
    private final int maxDeliveryCount;

    public PaymentMessageProcessor(
        PaymentMessageHandler paymentMessageHandler,
        IMessageReceiver messageReceiver,
        PaymentMessageParser paymentMessageParser,
        @Value("${azure.servicebus.payments.max-delivery-count}") int maxDeliveryCount
    ) {
        this.paymentMessageHandler = paymentMessageHandler;
        this.messageReceiver = messageReceiver;
        this.paymentMessageParser = paymentMessageParser;
        this.maxDeliveryCount = maxDeliveryCount;
    }

    /**
     * Reads and processes next message from the queue.
     *
     * @return false if there was no message to process. Otherwise true.
     */
    public boolean processNextMessage() throws ServiceBusException, InterruptedException {
        IMessage message = messageReceiver.receive();
        if (message != null) {
            MessageProcessingResult result = process(message);
            tryFinaliseProcessedMessage(message, result);
        }

        return message != null;
    }

    private MessageProcessingResult process(IMessage message) {
        log.info("Started processing message with ID {}", message.getMessageId());

        PaymentMessage payment = null;

        try {
            payment = paymentMessageParser.parse(message.getMessageBody());
            logMessageParsed(message, payment);
            paymentMessageHandler.handlePaymentMessage(payment);
            log.info("Processed message with ID {}. Envelope ID: {}", message.getMessageId(), payment.envelopeId);
            return new MessageProcessingResult(SUCCESS);
        } catch (InvalidMessageException ex) {
            log.error("Rejected message with ID {}, because it's invalid", message.getMessageId(), ex);
            return new MessageProcessingResult(UNRECOVERABLE_FAILURE, ex);
        } catch (PayHubClientException ex) {
            logMessageProcessingError(message, payment, ex);
            if (ex.getStatus() == HttpStatus.BAD_REQUEST.CONFLICT) {
                return new MessageProcessingResult(SUCCESS, ex);
            }
        } catch (Exception ex) {
            logMessageProcessingError(message, payment, ex);
            return new MessageProcessingResult(POTENTIALLY_RECOVERABLE_FAILURE);
        }

        return new MessageProcessingResult(POTENTIALLY_RECOVERABLE_FAILURE);
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
                log.info("Message with ID {} has been completed", message.getMessageId());
                break;
            case UNRECOVERABLE_FAILURE:
                deadLetterTheMessage(
                    message,
                    "Message processing error",
                    processingResult.exception.getMessage()
                );
                break;
            case POTENTIALLY_RECOVERABLE_FAILURE:
                // starts from 0
                int deliveryCount = (int) message.getDeliveryCount() + 1;

                if (deliveryCount < maxDeliveryCount) {
                    // do nothing - let the message lock expire
                    log.info(
                        "Allowing message with ID {} to return to queue (delivery attempt {})",
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

                break;
            default:
                throw new MessageProcessingException(
                    "Unknown message processing result type: " + processingResult.resultType
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
            description,
            ImmutableMap.of("deadLetteredAt", Instant.now().toString())
        );

        log.info("Message with ID {} has been dead-lettered", message.getMessageId());
    }

    private void logMessageFinaliseError(
        IMessage message,
        MessageProcessingResultType processingResultType,
        Exception ex) {
        log.error(
            "Failed to manage processed message with ID {}. Processing result: {}",
            message.getMessageId(),
            processingResultType,
            ex
        );
    }

    private void logMessageParsed(IMessage message, PaymentMessage payment) {
        log.info(
            "Parsed message. ID: {}, Envelope ID: {}, CCD Case Number: {}, Is Exception Record: {}, Jurisdiction: {}, "
                + "PO Box: {}, Document Control Numbers : {}",
            message.getMessageId(),
            payment.envelopeId,
            payment.ccdCaseNumber,
            payment.isExceptionRecord,
            payment.jurisdiction,
            payment.poBox,
            payment.payments

        );
    }

    private void logMessageProcessingError(IMessage message, PaymentMessage paymentMessage, Exception exception) {
        String baseMessage = String.format("Failed to process message with ID %s.", message.getMessageId());

        String fullMessage = paymentMessage != null
            ? baseMessage + String.format(
                " CCD Case Number: %s, Jurisdiction: %s",
                paymentMessage.ccdCaseNumber,
                paymentMessage.jurisdiction
            )
            : baseMessage;

        log.error(fullMessage, exception);
    }
}
