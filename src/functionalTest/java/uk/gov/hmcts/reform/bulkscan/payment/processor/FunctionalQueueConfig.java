package uk.gov.hmcts.reform.bulkscan.payment.processor;

import com.microsoft.azure.servicebus.QueueClient;
import com.microsoft.azure.servicebus.ReceiveMode;
import com.microsoft.azure.servicebus.primitives.ConnectionStringBuilder;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

public class FunctionalQueueConfig {

    @Value("${azure.servicebus.payments.write-connection-string}")
    private String paymentsQueueWriteConnectionString;

    @Bean("payments")
    @Profile("nosb")
    public QueueClient paymentsWriteClient() throws ServiceBusException, InterruptedException {
        return new QueueClient(
            new ConnectionStringBuilder(paymentsQueueWriteConnectionString),
            ReceiveMode.PEEKLOCK
        );
    }
}
