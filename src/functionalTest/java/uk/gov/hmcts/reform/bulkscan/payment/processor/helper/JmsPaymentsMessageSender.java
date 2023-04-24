package uk.gov.hmcts.reform.bulkscan.payment.processor.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.qpid.jms.JmsConnectionFactory;
import org.apache.qpid.jms.policy.JmsDefaultRedeliveryPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.bulkscan.payment.processor.model.CreatePaymentsCommand;

import javax.jms.ConnectionFactory;

@Service
@Profile("functional")
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
        String connection = String.format("amqp://localhost:%1s?amqp.idleTimeout=%2d", "5672", 30000);
        JmsConnectionFactory jmsConnectionFactory = new JmsConnectionFactory(connection);
        jmsConnectionFactory.setUsername("admin");
        jmsConnectionFactory.setPassword("admin");
        JmsDefaultRedeliveryPolicy jmsDefaultRedeliveryPolicy = new JmsDefaultRedeliveryPolicy();
        jmsDefaultRedeliveryPolicy.setMaxRedeliveries(3);
        jmsConnectionFactory.setRedeliveryPolicy(jmsDefaultRedeliveryPolicy);
        jmsConnectionFactory.setClientID("clientId");
        return new CachingConnectionFactory(jmsConnectionFactory);
    }
}
