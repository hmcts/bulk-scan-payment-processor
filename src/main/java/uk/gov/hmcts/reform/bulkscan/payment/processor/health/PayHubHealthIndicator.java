package uk.gov.hmcts.reform.bulkscan.payment.processor.health;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class PayHubHealthIndicator implements HealthIndicator {

    private static final Logger log = LoggerFactory.getLogger(PayHubHealthIndicator.class);

    private final String payHubUri;
    private final RestTemplate restTemplate;

    public PayHubHealthIndicator(
        @Value("${pay-hub.api.url}") String payHubUri,
        RestTemplate restTemplate
    ) {
        this.payHubUri = payHubUri;
        this.restTemplate = restTemplate;
    }

    @Override
    public Health health() {
        try {
            ResponseEntity<Object> response = restTemplate.getForEntity(payHubUri + "/health", Object.class);
            return response.getStatusCode() == (HttpStatus.OK) ? statusHealthy() : statusDown();
        } catch (Exception ex) {
            log.error("Exception occurred while checking Pay hub health", ex);
            return statusUnknown();
        }
    }

    private Health statusHealthy() {
        return Health.up().build();
    }

    private Health statusDown() {
        return Health.down().build();
    }

    private Health statusUnknown() {
        return Health.unknown().build();
    }
}
