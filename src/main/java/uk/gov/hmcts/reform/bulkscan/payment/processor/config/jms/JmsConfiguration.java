package uk.gov.hmcts.reform.bulkscan.payment.processor.config.jms;

import javax.jms.ConnectionFactory;
import javax.jms.Session;

import org.apache.activemq.RedeliveryPolicy;
import org.apache.qpid.jms.JmsConnectionFactory;
import org.apache.qpid.jms.policy.JmsRedeliveryPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jms.DefaultJmsListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;
import org.springframework.retry.annotation.Backoff;
import org.springframework.util.ErrorHandler;
import org.springframework.util.backoff.BackOff;
import org.springframework.util.backoff.FixedBackOff;

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

    @Bean
    public ConnectionFactory paymentsJmsConnectionFactory(@Value("${jms.application-name}") final String clientId) {
        String connection = String.format(amqpConnectionStringTemplate, namespace, idleTimeout);
        JmsConnectionFactory jmsConnectionFactory = new JmsConnectionFactory(connection);
        jmsConnectionFactory.setUsername(username);
        jmsConnectionFactory.setPassword(password);
        jmsConnectionFactory.setClientID(clientId);
        return new CachingConnectionFactory(jmsConnectionFactory);
    }

    @Bean
    public MessageConverter jsonJmsConverter() {
        MappingJackson2MessageConverter jackson = new MappingJackson2MessageConverter();
        //json is just text
        jackson.setTargetType(MessageType.TEXT);
        //each message should contain a property called _type and its value is the name of the class to serialize to
        jackson.setTypeIdPropertyName("_type");
        return jackson;
    }

    @Bean
    public JmsListenerContainerFactory<DefaultMessageListenerContainer> paymentsEventQueueContainerFactory(
        ConnectionFactory paymentsHearingsJmsConnectionFactory,
        DefaultJmsListenerContainerFactoryConfigurer defaultJmsListenerContainerFactoryConfigurer,
        JmsErrorHandler errorHandler) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(paymentsHearingsJmsConnectionFactory);
        factory.setReceiveTimeout(receiveTimeout);
        factory.setSessionTransacted(Boolean.TRUE);
        factory.setSessionAcknowledgeMode(Session.SESSION_TRANSACTED);
        factory.setErrorHandler(errorHandler);
        FixedBackOff fbo = new FixedBackOff();
        fbo.setMaxAttempts(3);
        fbo.setInterval(5000);
        factory.setBackOff(fbo);
        defaultJmsListenerContainerFactoryConfigurer.configure(factory, paymentsHearingsJmsConnectionFactory);
        return factory;
    }
}
