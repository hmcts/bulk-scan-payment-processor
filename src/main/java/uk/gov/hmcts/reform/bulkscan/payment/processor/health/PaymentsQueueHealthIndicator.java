package uk.gov.hmcts.reform.bulkscan.payment.processor.health;

import com.microsoft.azure.servicebus.IMessageReceiver;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class PaymentsQueueHealthIndicator implements HealthIndicator {
    private static final Logger log = LoggerFactory.getLogger(PaymentsQueueHealthIndicator.class);

    private final IMessageReceiver messageReceiver;

    public PaymentsQueueHealthIndicator(IMessageReceiver messageReceiver) {
        this.messageReceiver = messageReceiver;
    }

    @Override
    public Health health() {
        try {
            messageReceiver.peek();
            messageReceiver.close();
            return Health.up().build();
        } catch (InterruptedException | ServiceBusException e) {
            log.error("Error occurred while reading messages from payments queue", e);
            return Health.down().build();
        }
    }
}
