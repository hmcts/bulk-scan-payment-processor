package uk.gov.hmcts.reform.bulkscan.payment.processor;

import com.microsoft.azure.servicebus.IMessageReceiver;
import com.microsoft.azure.servicebus.QueueClient;
import com.microsoft.azure.servicebus.ReceiveMode;
import com.microsoft.azure.servicebus.primitives.ConnectionStringBuilder;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

import static org.mockito.Mockito.mock;

public class FunctionalQueueConfig {

    @Value("${azure.servicebus.payments.write-connection-string}")
    private String paymentsQueueWriteConnectionString;

    @Bean("payments")
    @Profile("functional")
    public QueueClient paymentsWriteClient() throws ServiceBusException, InterruptedException {
        return new QueueClient(
            new ConnectionStringBuilder(paymentsQueueWriteConnectionString),
            ReceiveMode.PEEKLOCK
        );
    }

    @Bean
    @Profile("functional") // apply only when Service Bus should not be used
    public IMessageReceiver testMessageReceiver() {
        return mock(IMessageReceiver.class);
    }
}
