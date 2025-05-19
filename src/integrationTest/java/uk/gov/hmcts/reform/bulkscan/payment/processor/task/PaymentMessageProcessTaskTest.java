package uk.gov.hmcts.reform.bulkscan.payment.processor.task;

import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import uk.gov.hmcts.reform.bulkscan.payment.processor.config.IntegrationTest;

import static org.mockito.Mockito.verify;

@SpringBootTest(properties =
    {"scheduling.task.consume-payments-queue.enabled=true",
        "scheduling.task.consume-payments-queue.time-interval-ms=20000"}
)
@IntegrationTest
class PaymentMessageProcessTaskTest {

    @MockitoSpyBean
    private  PaymentMessageProcessTask paymentMessageProcessTask;

    @MockitoBean
    private ServiceBusProcessorClient serviceBusProcessorClient;

    @Test
    void should_start_ServiceBusProcessorClient() {
        verify(serviceBusProcessorClient).start();
    }
}
