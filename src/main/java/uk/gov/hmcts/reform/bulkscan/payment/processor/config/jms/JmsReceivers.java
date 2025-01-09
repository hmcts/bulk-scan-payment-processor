//package uk.gov.hmcts.reform.bulkscan.payment.processor.config.jms;
//
//import com.fasterxml.jackson.core.JsonProcessingException;
//import jakarta.jms.JMSException;
//import jakarta.jms.Message;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.jms.annotation.JmsListener;
//import org.springframework.jms.core.JmsTemplate;
//import uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.JmsPaymentMessageProcessor;
//
///**
// * JMS receivers.
// */
//@Configuration()
//@ConditionalOnProperty(name = "jms.enabled", havingValue = "true")
//public class JmsReceivers {
//
//    private static final Logger log = LoggerFactory.getLogger(JmsReceivers.class);
//
//    private final JmsPaymentMessageProcessor jmsPaymentMessageProcessor;
//    private final JmsTemplate jmsTemplate;
//
//    /**
//     * Constructor.
//     * @param jmsPaymentMessageProcessor The JmsPaymentMessageProcessor
//     * @param jmsTemplate The JmsTemplate
//     */
//    public JmsReceivers(
//        JmsPaymentMessageProcessor jmsPaymentMessageProcessor,
//        JmsTemplate jmsTemplate
//    ) {
//        this.jmsPaymentMessageProcessor = jmsPaymentMessageProcessor;
//        this.jmsTemplate = jmsTemplate;
//    }
//
//    /**
//     * Receive message.
//     * @param message The message
//     * @throws JMSException JMSException
//     * @throws JsonProcessingException JsonProcessingException
//     */
//    @JmsListener(destination = "payments", containerFactory = "paymentsEventQueueContainerFactory")
//    public void receiveMessage(Message message) throws JMSException, JsonProcessingException {
//        String messageBody = ((jakarta.jms.TextMessage) message).getText();
//        log.info("Received Message {} on Service Bus. Delivery count is: {}",
//                 messageBody, message.getStringProperty("JMSXDeliveryCount"));
//        jmsPaymentMessageProcessor.processNextMessage(message, messageBody);
//        log.info("Message finished/completed");
//    }
//}
