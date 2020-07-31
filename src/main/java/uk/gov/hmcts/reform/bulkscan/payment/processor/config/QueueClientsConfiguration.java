package uk.gov.hmcts.reform.bulkscan.payment.processor.config;

import com.microsoft.azure.servicebus.ClientFactory;
import com.microsoft.azure.servicebus.IMessageReceiver;
import com.microsoft.azure.servicebus.ReceiveMode;
import com.microsoft.azure.servicebus.primitives.ConnectionStringBuilder;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@EnableConfigurationProperties(QueueConfigurationProperties.class)
@Profile("!functional & !integration")
public class QueueClientsConfiguration {

    private final QueueConfigurationProperties queueProperties;

    public QueueClientsConfiguration(QueueConfigurationProperties queueProperties) {
        this.queueProperties = queueProperties;
    }

    @Bean
    public IMessageReceiver paymentMessageReceiver() throws InterruptedException, ServiceBusException {
        return ClientFactory.createMessageReceiverFromConnectionStringBuilder(
            new ConnectionStringBuilder(
                queueProperties.getNamespace(),
                queueProperties.getQueueName(),
                queueProperties.getAccessKeyName(),
                queueProperties.getAccessKey()
            ),
            ReceiveMode.PEEKLOCK
        );
    }
}
