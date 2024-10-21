package uk.gov.hmcts.reform.bulkscan.payment.processor.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.jms.ConnectionFactory;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.RedeliveryPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.bulkscan.payment.processor.model.CreatePaymentsCommand;

@Service
@Profile("dev")
public class JmsPaymentsMessageSender {

    private static final Logger log = LoggerFactory.getLogger(JmsPaymentsMessageSender.class);

    private final ObjectMapper objectMapper = new ObjectMapper();

    public void send(CreatePaymentsCommand cmd) {
        try {
            final String messageContent = objectMapper.writeValueAsString(cmd);

            JmsTemplate jmsTemplate = new JmsTemplate();
            jmsTemplate.setConnectionFactory(getTestFactory());
            jmsTemplate.setReceiveTimeout(5000); // Set the receive timeout to 5 seconds

            jmsTemplate.convertAndSend("payments", messageContent);

            log.info(
                "Sent message to payments queue. Label: {}, Content: {}",
                cmd.label,
                messageContent
            );
        } catch (Exception ex) {
            throw new RuntimeException(
                "An error occurred when trying to publish message to payments queue.",
                ex
            );
        }
    }

    public ConnectionFactory getTestFactory() {
        String connection = "tcp://localhost:61616";
        ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory(connection);
        activeMQConnectionFactory.setUserName("admin");
        activeMQConnectionFactory.setPassword("admin");
        RedeliveryPolicy redeliveryPolicy = new RedeliveryPolicy();
        redeliveryPolicy.setMaximumRedeliveries(3);
        activeMQConnectionFactory.setRedeliveryPolicy(redeliveryPolicy);
        activeMQConnectionFactory.setClientID("clientId");
        return new CachingConnectionFactory(activeMQConnectionFactory);
    }
}
