package uk.gov.hmcts.reform.bulkscan.payment.processor.config.jms;

import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.Session;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.RedeliveryPolicy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jms.JmsProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.jms.support.converter.MessageConversionException;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.stereotype.Component;

/**
 * JMS configuration.
 */
@Configuration
@EnableJms
@ConditionalOnProperty(name = "jms.enabled", havingValue = "true")
public class JmsConfiguration {

    @Value("${jms.namespace}")
    private String namespace;

    @Value("${jms.username}")
    private String username;

    @Value("${jms.password}")
    private String password;

    @Value("${jms.receiveTimeout}")
    private Long receiveTimeout;

    @Value("${jms.idleTimeout}")
    private Long idleTimeout;

    @Value("${jms.amqp-connection-string-template}")
    public String amqpConnectionStringTemplate;

    /**
     * Jms properties.
     * @return The JmsProperties
     */
    @Primary
    @Bean
    public JmsProperties jmsProperties() {
        return new JmsProperties();
    }

    /**
     * Payments JMS connection factory.
     * @param clientId The client id
     * @return The connection factory
     */
    @Bean
    public ConnectionFactory paymentsJmsConnectionFactory(@Value("${jms.application-name}") final String clientId) {
        String connection = String.format(amqpConnectionStringTemplate, namespace, idleTimeout);
        ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory(connection);
        activeMQConnectionFactory.setUserName(username);
        activeMQConnectionFactory.setPassword(password);
        RedeliveryPolicy redeliveryPolicy = new RedeliveryPolicy();
        redeliveryPolicy.setMaximumRedeliveries(3);
        activeMQConnectionFactory.setRedeliveryPolicy(redeliveryPolicy);
        activeMQConnectionFactory.setClientID(clientId);
        return new CachingConnectionFactory(activeMQConnectionFactory);
    }

    /**
     * Payments hearings JMS connection factory.
     * @param connectionFactory The client id
     * @return The Jms template with a timeout of 5 seconds
     */
    @Bean
    public JmsTemplate jmsTemplate(ConnectionFactory connectionFactory) {
        JmsTemplate jmsTemplate = new JmsTemplate();
        jmsTemplate.setConnectionFactory(connectionFactory);
        jmsTemplate.setReceiveTimeout(5000); // Set the receive timeout to 5 seconds
        return jmsTemplate;
    }

    /**
     * Payments event queue container factory.
     * @param paymentsHearingsJmsConnectionFactory The connection factory
     * @param jmsProperties The JmsProperties
     * @return The jms listener container factory
     */
    @Bean
    public JmsListenerContainerFactory<DefaultMessageListenerContainer> paymentsEventQueueContainerFactory(
        ConnectionFactory paymentsHearingsJmsConnectionFactory,
        JmsProperties jmsProperties) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setSessionAcknowledgeMode(2);
        factory.setConnectionFactory(paymentsHearingsJmsConnectionFactory);
        factory.setReceiveTimeout(receiveTimeout);
        factory.setSessionTransacted(Boolean.TRUE);
        factory.setSessionAcknowledgeMode(Session.SESSION_TRANSACTED);
        factory.setMessageConverter(new CustomMessageConverter());
        factory.setPubSubDomain(jmsProperties.isPubSubDomain());
        return factory;
    }

    /**
     * Custom message converter.
     */
    @Component
    public static class CustomMessageConverter implements MessageConverter {

        /**
         * Convert object to message
         * @param object the object to convert
         * @param session the Session to use for creating a JMS Message
         * @throws JMSException if thrown by JMS API methods
         * @throws MessageConversionException if there is a problem with conversion
         * @return The message
         */
        @Override
        public Message toMessage(Object object, Session session) throws JMSException, MessageConversionException {
            return session.createTextMessage(object.toString());
        }

        /**
         * Convert message to object
         * @param message the message to convert
         * @throws MessageConversionException if there is a problem with conversion
         * @return The object
         */
        @Override
        public Object fromMessage(Message message) throws MessageConversionException {
            return message;
        }
    }
}
