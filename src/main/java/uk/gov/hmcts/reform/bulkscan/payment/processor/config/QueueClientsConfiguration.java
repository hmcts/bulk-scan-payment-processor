package uk.gov.hmcts.reform.bulkscan.payment.processor.config;

import com.microsoft.azure.servicebus.ClientFactory;
import com.microsoft.azure.servicebus.IMessageReceiver;
import com.microsoft.azure.servicebus.ReceiveMode;
import com.microsoft.azure.servicebus.primitives.ConnectionStringBuilder;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!functional & !integration")
public class QueueClientsConfiguration {

    @Bean
    @ConfigurationProperties(prefix = "azure.servicebus.payments")
    protected QueueConfigurationProperties paymentQueueConfig() {
        return new QueueConfigurationProperties();
    }

    @Bean
    public IMessageReceiver paymentMessageReceiver(
        QueueConfigurationProperties queueProperties
    ) throws InterruptedException, ServiceBusException {
        // looks like `from connection string builder` is not properly implemented and does not include auth
        var builder = new ConnectionStringBuilder(
            queueProperties.getNamespace(),
            queueProperties.getQueueName(),
            queueProperties.getAccessKeyName(),
            queueProperties.getAccessKey()
        );

        return ClientFactory.createMessageReceiverFromConnectionString(builder.toString(), ReceiveMode.PEEKLOCK);
    }
}
