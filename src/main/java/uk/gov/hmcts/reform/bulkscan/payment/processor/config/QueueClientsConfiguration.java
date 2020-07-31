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
        // looks like `from connection string builder` is not properly implemented and does not include auth
        var builder = new ConnectionStringBuilder(
            queueProperties.getNamespace(),
            queueProperties.getQueueName(),
            queueProperties.getAccessKeyName(),
            queueProperties.getAccessKey()
        );

        var string = builder.toString();
        org.slf4j.LoggerFactory.getLogger(QueueClientsConfiguration.class).warn("HERE IT IS: {}", string);
        return ClientFactory.createMessageReceiverFromConnectionString(string, ReceiveMode.PEEKLOCK);
    }
}
