package uk.gov.hmcts.reform.bulkscan.payment.processor;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import static org.mockito.Mockito.mock;

@Configuration
@Profile("functional")
public class FunctionalQueueConfig {

    @Bean("payments")
    public ServiceBusSenderClient paymentsWriteClient(
        @Value("${azure.servicebus.payments.namespace}") String namespace,
        @Value("${azure.servicebus.payments.write-access-key}") String accessKey,
        @Value("${azure.servicebus.payments.write-access-key-name}") String accessKeyName,
        @Value("${azure.servicebus.payments.queue-name}") String queueName
    ) {
        String connectionString = String.format(
            "Endpoint=sb://%s.servicebus.windows.net;SharedAccessKeyName=%s;SharedAccessKey=%s;",
            namespace,
            accessKeyName,
            accessKey
        );
        System.out.println("************** " + connectionString);

        return new ServiceBusClientBuilder()
            .connectionString(connectionString)
            .sender()
            .queueName(queueName)
            .buildClient();
    }

    @Bean
    public ServiceBusProcessorClient testServiceBusProcessorClient() {
        return mock(ServiceBusProcessorClient.class);
    }
}
