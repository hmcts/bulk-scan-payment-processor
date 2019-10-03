package uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.bulkscan.payment.processor.client.payhub.PayHubClient;
import uk.gov.hmcts.reform.bulkscan.payment.processor.client.payhub.request.PaymentRequest;
import uk.gov.hmcts.reform.bulkscan.payment.processor.client.payhub.response.PaymentResult;
import uk.gov.hmcts.reform.bulkscan.payment.processor.data.producer.SamplePaymentMessageData;
import uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.handler.PaymentMessageHandler;
import uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.model.PaymentMessage;

import java.util.Optional;

import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PaymentMessageHandlerTest {

    @Mock
    private PayHubClient payHubClient;

    @Mock
    private AuthTokenGenerator s2sTokenGenerator;

    @Mock
    private PaymentRequestMapper requestMapper;

    private PaymentMessageHandler messageHandler;

    @BeforeEach
    void setUp() {
        messageHandler = new PaymentMessageHandler(s2sTokenGenerator, requestMapper, payHubClient);
    }

    @Test
    public void should_call_payhub_api_for_successful_payment_message() {
        // given
        PaymentMessage message = SamplePaymentMessageData.paymentMessage("1234", true);

        when(s2sTokenGenerator.generate()).thenReturn("test-service");
        PaymentRequest request = new PaymentRequest("1234", singletonList("1234"), true, "test-siteId");

        when(requestMapper.mapPaymentMessage(message)).thenReturn(request);
        when(payHubClient.postPayments(any(), eq(request)))
            .thenReturn(ResponseEntity.of(Optional.of(new PaymentResult(singletonList("1234")))));

        // when
        messageHandler.handlePaymentMessage(message);

        // then
        verify(requestMapper).mapPaymentMessage(message);
        verify(s2sTokenGenerator).generate();
        verify(payHubClient).postPayments(anyString(), any(PaymentRequest.class));
    }

}
