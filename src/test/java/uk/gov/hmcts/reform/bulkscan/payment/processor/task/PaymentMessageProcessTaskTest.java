package uk.gov.hmcts.reform.bulkscan.payment.processor.task;

import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import io.github.netmikey.logunit.api.LogCapturer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.slf4j.event.Level.ERROR;
import static org.slf4j.event.Level.INFO;


@ExtendWith(MockitoExtension.class)
class PaymentMessageProcessTaskTest {
    @RegisterExtension
    public LogCapturer logs = LogCapturer.create().captureForType(PaymentMessageProcessTask.class);

    @Mock
    private ServiceBusProcessorClient serviceBusProcessorClient;

    @InjectMocks
    private PaymentMessageProcessTask queueConsumeTask;

    @BeforeEach
    void setUp() {
        queueConsumeTask = new PaymentMessageProcessTask(
            serviceBusProcessorClient
        );
    }

    @Test
    void should_log_when_listener_is_not_working() {
        given(serviceBusProcessorClient.isRunning()).willReturn(false);
        queueConsumeTask.checkServiceBusProcessorClient();

        assertThat(logs.assertContains(
            event -> event.getLevel() == ERROR, "No ERROR level log").getMessage())
            .isEqualTo("Payments queue consume listener is NOT running!!!");
    }

    @Test
    void should_log_when_listener_is_working() {
        given(serviceBusProcessorClient.isRunning()).willReturn(true);
        queueConsumeTask.checkServiceBusProcessorClient();
        assertThat(logs.assertContains(
            event -> event.getLevel() == INFO, "No INFO level log").getMessage())
            .isEqualTo("Payments queue consume listener is working.");
    }
}
