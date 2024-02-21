package uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus;

import com.azure.core.util.BinaryData;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.exceptions.UnknownMessageProcessingResultException;
import uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.handler.MessageProcessingResult;

import jakarta.jms.JMSException;
import jakarta.jms.Message;

/**
 * For LOCAL Dev Only. For AAT and upwards Azure Service Bus is used
 */
@Service
@Profile("!functional & !integration")
public class JmsPaymentMessageProcessor {
    private static final Logger log = LoggerFactory.getLogger(JmsPaymentMessageProcessor.class);

    private final PaymentCommands paymentCommands;

    public JmsPaymentMessageProcessor(
        PaymentCommands paymentCommands
    ) {
        this.paymentCommands = paymentCommands;
    }

    /**
     * Reads and processes next message from the queue.
     * return false if there was no message to process. Otherwise true.
     */
    public void processNextMessage(Message message, String messageBody) throws JsonProcessingException, JMSException {
        if (message != null && !messageBody.isEmpty()) {
            JsonNode messageBodyAsNode = new ObjectMapper().readTree(messageBody);
            if (messageBodyAsNode.get("label").asText() == null) {
                log.error("No label provided in the body of the request");
                message.acknowledge();
            } else {
                String label = messageBodyAsNode.get("label").asText();
                switch (label) {
                    case "CREATE" -> {
                        MessageProcessingResult result = paymentCommands.processCreateCommand(
                            message.getJMSMessageID(),
                            BinaryData.fromString(messageBody)
                        );
                        tryFinaliseProcessedMessage(message, result);
                    }
                    case "UPDATE" -> {
                        var updateResult = paymentCommands.processUpdateCommand(
                            message.getJMSMessageID(),
                            BinaryData.fromString(messageBody)
                        );
                        tryFinaliseProcessedMessage(message, updateResult);
                    }
                    default -> {
                        log.error("Unknown label found: {} for ID: {}", label, message.getJMSMessageID());
                        message.acknowledge();
                    }
                }
            }
        } else {
            log.info("""
                     No payment messages to process by payment processor!!
                     """);
        }

    }

    private void tryFinaliseProcessedMessage(
        Message message,
        MessageProcessingResult processingResult
    ) throws JMSException {
        try {
            finaliseProcessedMessage(message, processingResult);
        } catch (Exception ex) {
            paymentCommands.logMessageFinaliseError(message.getJMSMessageID(),
                                                    processingResult.resultType, ex
            );
        }
    }

    private void finaliseProcessedMessage(
        Message messageContext,
        MessageProcessingResult processingResult
    ) throws JMSException {
        switch (processingResult.resultType) {
            case SUCCESS -> {
                log.info("Payment Message with ID {} has been completed", messageContext.getJMSMessageID());
                messageContext.acknowledge();
            }
            case UNRECOVERABLE_FAILURE -> {
                log.error("Payment Message processing error: {}", processingResult.exception.getMessage());
                messageContext.acknowledge();
            }
            case POTENTIALLY_RECOVERABLE_FAILURE -> throw new UnknownMessageProcessingResultException(
                "Potential recovery error found...retrying");
            default -> throw new UnknownMessageProcessingResultException(
                "Unknown payment message processing result type: " + processingResult.resultType
            );
        }
    }
}
