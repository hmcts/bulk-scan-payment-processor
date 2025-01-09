//package uk.gov.hmcts.reform.bulkscan.payment.processor.task;
//
//import com.azure.messaging.servicebus.ServiceBusProcessorClient;
//import org.junit.jupiter.api.Test;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.boot.test.mock.mockito.SpyBean;
//import uk.gov.hmcts.reform.bulkscan.payment.processor.config.IntegrationTest;
//
//import static org.mockito.Mockito.verify;
//
//@SpringBootTest(properties =
//    {"scheduling.task.consume-payments-queue.enabled=true",
//        "scheduling.task.consume-payments-queue.time-interval-ms=20000"}
//)
//@IntegrationTest
//class PaymentMessageProcessTaskTest {
//
//    @SpyBean
//    private  PaymentMessageProcessTask paymentMessageProcessTask;
//
//    @MockBean
//    private ServiceBusProcessorClient serviceBusProcessorClient;
//
//    @Test
//    void should_start_ServiceBusProcessorClient() {
//        verify(serviceBusProcessorClient).start();
//    }
//}
