package uk.gov.hmcts.reform.bulkscan.payment.processor.config.jms;

import javax.jms.JMSException;
import javax.jms.Message;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.JmsPaymentMessageProcessor;
import uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.PaymentMessageProcessor;

@Configuration()
@ConditionalOnProperty(name = "jms.enabled", havingValue = "true")
public class JmsReceivers {

    private static final Logger log = LoggerFactory.getLogger(JmsReceivers.class);

    private final JmsPaymentMessageProcessor jmsPaymentMessageProcessor;
    private final JmsTemplate jmsTemplate;

    public JmsReceivers(
        JmsPaymentMessageProcessor jmsPaymentMessageProcessor,
        JmsTemplate jmsTemplate
    ) {
        this.jmsPaymentMessageProcessor = jmsPaymentMessageProcessor;
        this.jmsTemplate = jmsTemplate;
    }

    @JmsListener(destination = "payments", containerFactory = "paymentsEventQueueContainerFactory")
    public void receiveMessage(Message message) throws JMSException, JsonProcessingException {
        String messageBody = ((javax.jms.TextMessage) message).getText();
        log.info("Received Message {} on Service Bus. Delivery count is: {}",
                 messageBody, message.getStringProperty("JMSXDeliveryCount"));

        // When an exception is thrown that isn't a JMSException, this will retry. We can raise an exception to say
        // in the case of a dead letter to complete the message, as we are in the local space

        jmsPaymentMessageProcessor.processNextMessage(message, messageBody);


//        CreatePaymentMessage paymentInfo = new Gson().fromJson(message, CreatePaymentMessage.class);

//        throw new JMSException("bla de blar");

        // to write to a queue
        // jmsTemplate.convertAndSend("envelopes", "Hello, World!");




        log.info("Message finished/completed");
    }
}
