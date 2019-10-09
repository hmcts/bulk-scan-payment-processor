package uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.bulkscan.payment.processor.client.payhub.PayHubClient;
import uk.gov.hmcts.reform.bulkscan.payment.processor.client.payhub.request.CreatePaymentRequest;
import uk.gov.hmcts.reform.bulkscan.payment.processor.client.payhub.response.CreatePaymentResponse;
import uk.gov.hmcts.reform.bulkscan.payment.processor.data.producer.SamplePaymentMessageData;
import uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.handler.PaymentMessageHandler;
import uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.model.PaymentMessage;

import java.util.Optional;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
        CreatePaymentRequest request = new CreatePaymentRequest("1234", singletonList("1234"), true, "test-siteId");

        when(requestMapper.mapPaymentMessage(message)).thenReturn(request);
        when(payHubClient.createPayment(any(), eq(request)))
            .thenReturn(ResponseEntity.of(Optional.of(new CreatePaymentResponse(singletonList("1234")))));

        // when
        CreatePaymentResponse result = messageHandler.handlePaymentMessage(message);

        // then
        assertThat(result).isNotNull();
        assertThat(result.paymentDcns).hasSize(1).contains("1234");
    }
}
