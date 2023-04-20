package uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus;

import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;
import com.azure.messaging.servicebus.models.DeadLetterOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.exceptions.UnknownMessageProcessingResultException;
import uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.handler.MessageProcessingResult;

@Service
@Profile("!functional & !integration")
public class PaymentMessageProcessor {

    private static final Logger log = LoggerFactory.getLogger(PaymentMessageProcessor.class);

    private final PaymentCommands paymentCommands;
    private final int maxDeliveryCount;

    public PaymentMessageProcessor(
        PaymentCommands paymentCommands,
        @Value("${azure.servicebus.payments.max-delivery-count}") int maxDeliveryCount
    ) {
        this.paymentCommands = paymentCommands;
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
                        MessageProcessingResult result = paymentCommands.processCreateCommand(message.getMessageId(), message.getBody());
                        tryFinaliseProcessedMessage(serviceBusReceivedMessageContext, result);
                    }
                    case "UPDATE" -> {
                        var updateResult = paymentCommands.processUpdateCommand(message.getMessageId(), message.getBody());
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

    private void tryFinaliseProcessedMessage(
        ServiceBusReceivedMessageContext messageContext,
        MessageProcessingResult processingResult
    ) {
        try {
            finaliseProcessedMessage(messageContext, processingResult);
        } catch (Exception ex) {
            paymentCommands.logMessageFinaliseError(messageContext.getMessage().getMessageId(),
                                                    processingResult.resultType, ex);
        }
    }

    private void finaliseProcessedMessage(
        ServiceBusReceivedMessageContext messageContext,
        MessageProcessingResult processingResult
    ) {
        ServiceBusReceivedMessage message = messageContext.getMessage();
        switch (processingResult.resultType) {
            case SUCCESS -> {
                messageContext.complete();
                log.info("Payment Message with ID {} has been completed", message.getMessageId());
            }
            case UNRECOVERABLE_FAILURE -> deadLetterTheMessage(
                messageContext,
                "Payment Message processing error",
                processingResult.exception.getMessage()
            );
            case POTENTIALLY_RECOVERABLE_FAILURE -> deadLetterIfMaxDeliveryCountIsReached(messageContext);
            default -> throw new UnknownMessageProcessingResultException(
                "Unknown payment message processing result type: " + processingResult.resultType
            );
        }
    }

    private void deadLetterIfMaxDeliveryCountIsReached(ServiceBusReceivedMessageContext messageContext) {

        ServiceBusReceivedMessage message = messageContext.getMessage();
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
        ServiceBusReceivedMessage message = messageContext.getMessage();
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
}
