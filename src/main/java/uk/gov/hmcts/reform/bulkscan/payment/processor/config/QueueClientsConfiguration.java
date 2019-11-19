package uk.gov.hmcts.reform.bulkscan.payment.processor.config;

import com.microsoft.azure.servicebus.ClientFactory;
import com.microsoft.azure.servicebus.IMessageReceiver;
import com.microsoft.azure.servicebus.ReceiveMode;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!functional & !integration")
public class QueueClientsConfiguration {

    @Bean
    public IMessageReceiver paymentMessageReceiver(
        @Value("${azure.servicebus.payments.connection-string}") String connectionString
    ) throws InterruptedException, ServiceBusException {
        return ClientFactory.createMessageReceiverFromConnectionString(
            connectionString,
            ReceiveMode.PEEKLOCK
        );
    }

}
