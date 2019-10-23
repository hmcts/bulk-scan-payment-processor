package uk.gov.hmcts.reform.bulkscan.payment.processor;

import com.microsoft.azure.servicebus.ClientFactory;
import com.microsoft.azure.servicebus.IMessageReceiver;
import com.microsoft.azure.servicebus.ReceiveMode;
import com.microsoft.azure.servicebus.primitives.ConnectionStringBuilder;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import com.typesafe.config.ConfigFactory;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThatCode;

@TestPropertySource("classpath:application.conf")
class PaymentsQueueHealthTest {

    private static final String CONNECTION_STRING = ConfigFactory.load()
        .getString("payments-queue-read-connection-string");

    @Test
    void can_read_messages_from_payments_queue() throws ServiceBusException, InterruptedException {
        IMessageReceiver paymentsReceiver = null;
        try {
            paymentsReceiver = ClientFactory.createMessageReceiverFromConnectionStringBuilder(
                new ConnectionStringBuilder(CONNECTION_STRING),
                ReceiveMode.PEEKLOCK
            );

            assertThatCode(paymentsReceiver::peek).doesNotThrowAnyException();
        } finally {
            if (paymentsReceiver != null) {
                paymentsReceiver.close();
            }
        }
    }

}


