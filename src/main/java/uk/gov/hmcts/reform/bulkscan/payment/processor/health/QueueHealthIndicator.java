package uk.gov.hmcts.reform.bulkscan.payment.processor.health;

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

    public QueueHealthIndicator() {
    }

    @Override
    public Health health() {
        try {
            return Health.up().build();
        } catch (Exception e) {
            log.error("Error occurred while reading messages from payments queue", e);
            return Health.down().withException(e).build();
        }
    }
}
