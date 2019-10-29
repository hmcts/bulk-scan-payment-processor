package uk.gov.hmcts.reform.bulkscan.payment.processor.config;

import com.microsoft.azure.servicebus.ClientFactory;
import com.microsoft.azure.servicebus.IMessageReceiver;
import com.microsoft.azure.servicebus.ReceiveMode;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!nosb") // do not register handler for the nosb (test) profile
public class QueueClientsConfiguration {
    private static final Logger log = LoggerFactory.getLogger(QueueClientsConfiguration.class);

    @Bean
    public IMessageReceiver paymentMessageReceiver(
        @Value("${azure.servicebus.payments.connection-string}") String connectionString
    ) throws InterruptedException, ServiceBusException {
        log.info("payments-connection-string: " + connectionString);
        return ClientFactory.createMessageReceiverFromConnectionString(
            connectionString,
            ReceiveMode.PEEKLOCK
        );
    }

}
