package uk.gov.hmcts.reform.bulkscan.payment.processor;

import com.microsoft.azure.servicebus.IMessageReceiver;
import com.microsoft.azure.servicebus.QueueClient;
import com.microsoft.azure.servicebus.ReceiveMode;
import com.microsoft.azure.servicebus.primitives.ConnectionStringBuilder;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import static org.mockito.Mockito.mock;

@Configuration
@Profile("functional")
public class FunctionalQueueConfig {

    @Bean("payments")
    public QueueClient paymentsWriteClient(
        @Value("${azure.servicebus.payments.namespace}") String namespace,
        @Value("${azure.servicebus.payments.write-access-key}") String accessKey,
        @Value("${azure.servicebus.payments.write-access-key-name}") String accessKeyName,
        @Value("${azure.servicebus.payments.queue-name}") String queueName
    ) throws ServiceBusException, InterruptedException {
        return new QueueClient(
            new ConnectionStringBuilder(namespace, queueName, accessKeyName, accessKey),
            ReceiveMode.PEEKLOCK
        );
    }

    @Bean
    public IMessageReceiver testMessageReceiver() {
        return mock(IMessageReceiver.class);
    }
}
