package uk.gov.hmcts.reform.bulkscan.payment.processor.health;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class QueueHealthIndicatorTest {

    private HealthIndicator healthIndicator;

    @BeforeEach
    void setUp() {
        healthIndicator = new QueueHealthIndicator();
    }

    @Test
    void should_be_healthy_if_peek_queue_message_does_not_throw_exception() throws Exception {
        assertThat(healthIndicator.health().getStatus()).isEqualTo(Status.UP);
    }


}
