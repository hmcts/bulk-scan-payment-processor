package uk.gov.hmcts.reform.bulkscan.payment.processor;

import com.microsoft.azure.servicebus.IMessageReceiver;
import com.microsoft.azure.servicebus.QueueClient;
import com.microsoft.azure.servicebus.ReceiveMode;
import com.microsoft.azure.servicebus.primitives.ConnectionStringBuilder;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.bulkscan.payment.processor.client.payhub.PayHubClient;
import uk.gov.hmcts.reform.bulkscan.payment.processor.client.payhub.request.CaseReferenceRequest;
import uk.gov.hmcts.reform.bulkscan.payment.processor.client.payhub.request.CreatePaymentRequest;
import uk.gov.hmcts.reform.bulkscan.payment.processor.client.payhub.response.CreatePaymentResponse;

import static org.mockito.Mockito.mock;

public class FunctionalQueueConfig {

    @Value("${azure.servicebus.payments.write-connection-string}")
    private String paymentsQueueWriteConnectionString;

    @Bean("payments")
    @Profile("nosb")
    public QueueClient paymentsWriteClient() throws ServiceBusException, InterruptedException {
        return new QueueClient(
            new ConnectionStringBuilder(paymentsQueueWriteConnectionString),
            ReceiveMode.PEEKLOCK
        );
    }

    @Bean
    @Profile("nosb") // apply only when Service Bus should not be used
    public IMessageReceiver testMessageReceiver() {
        return mock(IMessageReceiver.class);
    }

    @Bean
    @Profile("nosb") // apply only when Service Bus should not be used
    PayHubClient payHubClient() {
        return new PayHubClient() {
            @Override
            public ResponseEntity<CreatePaymentResponse> createPayment(
                String serviceAuthorisation,
                CreatePaymentRequest paymentRequest
            ) {
                return null;
            }

            @Override
            public ResponseEntity<Void> updateCaseReference(
                String serviceAuthorisation,
                String exceptionReference,
                CaseReferenceRequest caseReferenceRequest
            ) {
                return null;
            }
        };
    }
}
