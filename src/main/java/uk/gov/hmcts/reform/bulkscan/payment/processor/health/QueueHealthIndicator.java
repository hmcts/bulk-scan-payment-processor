package uk.gov.hmcts.reform.bulkscan.payment.processor.health;

import com.microsoft.azure.servicebus.IMessageReceiver;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!functional & !integration")
public class QueueHealthIndicator implements HealthIndicator {
    private static final Logger log = LoggerFactory.getLogger(QueueHealthIndicator.class);

    private final IMessageReceiver messageReceiver;

    public QueueHealthIndicator(IMessageReceiver messageReceiver) {
        this.messageReceiver = messageReceiver;
    }

    @Override
    public Health health() {
        try {
            messageReceiver.peek();
            return Health.up().build();
        } catch (InterruptedException e) {
            log.error("Error occurred while reading messages from payments queue", e);
            Thread.currentThread().interrupt();
            return Health.down().withException(e).build();
        } catch (ServiceBusException e) {
            log.error("Error occurred while reading messages from payments queue", e);
            return Health.down().withException(e).build();
        }
    }
}
