package uk.gov.hmcts.reform.bulkscan.payment.processor.config;

import javax.jms.ConnectionFactory;
import javax.jms.Session;

import org.apache.activemq.util.FactoryFinder;
import org.apache.qpid.jms.JmsConnectionFactory;
import org.apache.qpid.jms.provider.ProviderFactory;
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
        System.out.println(ProviderFactory.class.getPackage().getName());
        JmsConnectionFactory jmsConnectionFactory = new JmsConnectionFactory(connection);
        jmsConnectionFactory.setUsername(username);
        jmsConnectionFactory.setPassword(password);
        jmsConnectionFactory.setClientID("balls");
        return new CachingConnectionFactory(jmsConnectionFactory);
    }

    @Bean
    public JmsListenerContainerFactory<DefaultMessageListenerContainer> paymentsEventQueueContainerFactory(
        ConnectionFactory tribunalsHearingsJmsConnectionFactory,
        DefaultJmsListenerContainerFactoryConfigurer defaultJmsListenerContainerFactoryConfigurer) {

        System.out.println("goes into here as well");

        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(tribunalsHearingsJmsConnectionFactory);
        factory.setReceiveTimeout(receiveTimeout);
        factory.setSessionTransacted(Boolean.TRUE);
        factory.setSessionAcknowledgeMode(Session.SESSION_TRANSACTED);
        factory.setMessageConverter(new JsonMessageConverter());
        defaultJmsListenerContainerFactoryConfigurer.configure(factory, tribunalsHearingsJmsConnectionFactory);
        return factory;
    }

}
