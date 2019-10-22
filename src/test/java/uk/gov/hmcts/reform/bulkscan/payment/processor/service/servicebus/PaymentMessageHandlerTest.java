package uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.bulkscan.payment.processor.client.payhub.PayHubClient;
import uk.gov.hmcts.reform.bulkscan.payment.processor.client.payhub.PayHubClientException;
import uk.gov.hmcts.reform.bulkscan.payment.processor.client.payhub.request.CaseReferenceRequest;
import uk.gov.hmcts.reform.bulkscan.payment.processor.client.payhub.request.CreatePaymentRequest;
import uk.gov.hmcts.reform.bulkscan.payment.processor.client.payhub.response.CreatePaymentResponse;
import uk.gov.hmcts.reform.bulkscan.payment.processor.data.producer.SamplePaymentMessageData;
import uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.handler.PaymentMessageHandler;
import uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.model.CreatePaymentMessage;
import uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.model.UpdatePaymentMessage;

import java.util.Optional;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
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
        CreatePaymentMessage message = SamplePaymentMessageData.paymentMessage("1234", true);
        String s2sToken = "s2sToken1";

        when(s2sTokenGenerator.generate()).thenReturn(s2sToken);
        CreatePaymentRequest request = new CreatePaymentRequest("1234", singletonList("1234"), true, "test-siteId");

        when(requestMapper.mapPaymentMessage(message)).thenReturn(request);
        when(payHubClient.createPayment(any(), eq(request)))
            .thenReturn(ResponseEntity.of(Optional.of(new CreatePaymentResponse(singletonList("1234")))));

        // when
        messageHandler.handlePaymentMessage(message);

        // then
        verify(payHubClient).createPayment(s2sToken, request);
    }

    @Test
    public void should_call_payhub_api_to_assign_case_ref() {
        // given
        UpdatePaymentMessage message = new UpdatePaymentMessage(
            "env-id-12321",
            "Divorce",
            "exp-21321",
            "cas-ref-9999"
        );

        when(s2sTokenGenerator.generate()).thenReturn("test-service");

        when(payHubClient.updateCaseReference(
            eq("test-service"), eq("exp-21321"), any(CaseReferenceRequest.class))
        ).thenReturn(ResponseEntity.status(200).build());

        // when
        messageHandler.updatePaymentCaseReference(message);

        // then
        ArgumentCaptor<CaseReferenceRequest> requestCaptor = ArgumentCaptor.forClass(CaseReferenceRequest.class);

        verify(payHubClient).updateCaseReference(any(), any(), requestCaptor.capture());
        CaseReferenceRequest req = requestCaptor.getValue();
        CaseReferenceRequest expectedRequest = new CaseReferenceRequest("cas-ref-9999");
        assertThat(req).isEqualToComparingFieldByFieldRecursively(expectedRequest);
    }


    @Test
    public void should_throw_exception_if_payhub_api_assign_case_ref_fails() {
        // given
        UpdatePaymentMessage message = new UpdatePaymentMessage(
            "env-id-12321",
            "Divorce",
            "exp-21321",
            "cas-ref-9999"
        );

        when(s2sTokenGenerator.generate()).thenReturn("test-service");

        when(payHubClient.updateCaseReference(
            eq("test-service"), eq("exp-21321"), any(CaseReferenceRequest.class))
        ).thenThrow(new PayHubClientException(new HttpClientErrorException(HttpStatus.BAD_REQUEST, "BAD_REQUEST")));

        // when
        assertThatThrownBy(
            () -> messageHandler.updatePaymentCaseReference(message))
            .isInstanceOf(PayHubClientException.class);

    }
}
