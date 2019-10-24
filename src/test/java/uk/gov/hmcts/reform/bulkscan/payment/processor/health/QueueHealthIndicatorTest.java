package uk.gov.hmcts.reform.bulkscan.payment.processor.health;

import com.microsoft.azure.servicebus.IMessageReceiver;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class QueueHealthIndicatorTest {

    @Mock
    private IMessageReceiver receiver;

    private HealthIndicator healthIndicator;

    @BeforeEach
    void setUp() {
        healthIndicator = new QueueHealthIndicator(receiver);
    }

    @Test
    void should_be_healthy_if_peek_queue_message_does_not_throw_exception() throws Exception {
        given(receiver.peek()).willReturn(null);
        assertThat(healthIndicator.health().getStatus()).isEqualTo(Status.UP);
    }

    @Test
    void should_be_unhealthy_when_peek_queue_message_throws_interrupted_exception() throws Exception {
        given(receiver.peek()).willThrow(InterruptedException.class);
        assertThat(healthIndicator.health().getStatus()).isEqualTo(Status.DOWN);
    }

    @Test
    void should_be_unhealthy_when_peek_queue_message_throws_service_bus_exception() throws Exception {
        given(receiver.peek()).willThrow(ServiceBusException.class);
        assertThat(healthIndicator.health().getStatus()).isEqualTo(Status.DOWN);
    }

}
