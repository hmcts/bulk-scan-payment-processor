//package uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus;
//
//import com.azure.core.util.BinaryData;
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import jakarta.jms.JMSException;
//import jakarta.jms.Message;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.context.annotation.Profile;
//import org.springframework.stereotype.Service;
//import uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.handler.MessageProcessingResult;
//
///**
// * For LOCAL Dev Only. For AAT and upwards Azure Service Bus is used
// */
//@Service
//@Profile("dev")
//public class JmsPaymentMessageProcessor {
//    private static final Logger log = LoggerFactory.getLogger(JmsPaymentMessageProcessor.class);
//
//    private final PaymentCommands paymentCommands;
//
//    /**
//     * Constructor for the JmsPaymentMessageProcessor.
//     * @param paymentCommands The payment commands
//     */
//    public JmsPaymentMessageProcessor(
//        PaymentCommands paymentCommands
//    ) {
//        this.paymentCommands = paymentCommands;
//    }
//
//    /**
//     * Process the next message.
//     * Reads and processes next message from the queue.
//     * return false if there was no message to process. Otherwise true.
//     * @param message The message
//     * @param messageBody The message body
//     * @throws JsonProcessingException If there is a JSON processing error
//     * @throws JMSException If there is a JMS error
//     */
//    public void processNextMessage(Message message, String messageBody) throws JsonProcessingException, JMSException {
//        if (message != null && !messageBody.isEmpty()) {
//            JsonNode messageBodyAsNode = new ObjectMapper().readTree(messageBody);
//            if (messageBodyAsNode.get("label").asText() == null) {
//                log.error("No label provided in the body of the request");
//                message.acknowledge();
//            } else {
//                String label = messageBodyAsNode.get("label").asText();
//                switch (label) {
//                    case "CREATE" -> {
//                        MessageProcessingResult result = paymentCommands.processCreateCommand(
//                            message.getJMSMessageID(),
//                            BinaryData.fromString(messageBody)
//                        );
//                        tryFinaliseProcessedMessage(message, result);
//                    }
//                    case "UPDATE" -> {
//                        var updateResult = paymentCommands.processUpdateCommand(
//                            message.getJMSMessageID(),
//                            BinaryData.fromString(messageBody)
//                        );
//                        tryFinaliseProcessedMessage(message, updateResult);
//                    }
//                    default -> {
//                        log.error("Unknown label found: {} for ID: {}", label, message.getJMSMessageID());
//                        message.acknowledge();
//                    }
//                }
//            }
//        } else {
//            log.info("""
//                     No payment messages to process by payment processor!!
//                     """);
//        }
//
//    }
//
//    /**
//     * Try to finalise the processed message.
//     * @param message The message
//     * @param processingResult The processing result
//     * @throws JMSException If there is a JMS error
//     */
//    private void tryFinaliseProcessedMessage(
//        Message message,
//        MessageProcessingResult processingResult
//    ) throws JMSException {
//        try {
//            finaliseProcessedMessage(message, processingResult);
//        } catch (Exception ex) {
//            paymentCommands.logMessageFinaliseError(message.getJMSMessageID(),
//                                                    processingResult.resultType, ex
//            );
//        }
//    }
//
//    /**
//     * Finalise the processed message.
//     * @param messageContext The message context
//     * @param processingResult The processing result
//     * @throws JMSException If there is a JMS error
//     */
//    private void finaliseProcessedMessage(
//        Message messageContext,
//        MessageProcessingResult processingResult
//    ) throws JMSException {
//        switch (processingResult.resultType) {
//            case SUCCESS -> {
//                log.info("Payment Message with ID {} has been completed", messageContext.getJMSMessageID());
//                messageContext.acknowledge();
//            }
//            case UNRECOVERABLE_FAILURE -> {
//                log.error("Payment Message processing error: {}", processingResult.exception.getMessage());
//                messageContext.acknowledge();
//            }
//            case POTENTIALLY_RECOVERABLE_FAILURE -> throw new UnknownMessageProcessingResultException(
//                "Potential recovery error found...retrying");
//            default -> throw new UnknownMessageProcessingResultException(
//                "Unknown payment message processing result type: " + processingResult.resultType
//            );
//        }
//    }
//}
