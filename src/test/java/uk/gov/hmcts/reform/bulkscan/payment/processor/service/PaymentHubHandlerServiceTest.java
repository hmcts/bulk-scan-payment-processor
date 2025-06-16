package uk.gov.hmcts.reform.bulkscan.payment.processor.service;

import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.bulkscan.payment.processor.ccd.CcdClient;
import uk.gov.hmcts.reform.bulkscan.payment.processor.client.payhub.PayHubClient;
import uk.gov.hmcts.reform.bulkscan.payment.processor.client.payhub.request.CaseReferenceRequest;
import uk.gov.hmcts.reform.bulkscan.payment.processor.client.payhub.request.CreatePaymentRequest;
import uk.gov.hmcts.reform.bulkscan.payment.processor.client.payhub.response.CreatePaymentResponse;
import uk.gov.hmcts.reform.bulkscan.payment.processor.errorhandling.exception.PayHubCallException;
import uk.gov.hmcts.reform.bulkscan.payment.processor.models.CreatePayment;
import uk.gov.hmcts.reform.bulkscan.payment.processor.models.PaymentInfo;
import uk.gov.hmcts.reform.bulkscan.payment.processor.models.UpdatePayment;

import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentHubHandlerServiceTest {

    @Mock
    private PayHubClient payHubClient;

    @Mock
    private AuthTokenGenerator s2sTokenGenerator;

    @Mock
    private PaymentRequestMapper requestMapper;

    @Mock
    private CcdClient ccdClient;

    private PaymentHubHandlerService messageHandler;

    private final FeignException.InternalServerError mockFeignException =
            mock(FeignException.InternalServerError.class);


    @BeforeEach
    void setUp() {
        messageHandler = new PaymentHubHandlerService(s2sTokenGenerator, requestMapper, payHubClient, ccdClient);
    }

    @Test
    void should_call_payhub_api_to_update_payment_case_reference() {
        UpdatePayment updatePayment = new UpdatePayment(
            "env-id-12321",
            "Divorce",
            "exp-21321",
            "cas-ref-9999"
        );

        when(s2sTokenGenerator.generate()).thenReturn("test-service");

        when(payHubClient.updateCaseReference(
            eq("test-service"), eq("exp-21321"), any(CaseReferenceRequest.class))
        ).thenReturn(ResponseEntity.status(200).build());

        messageHandler.updatePaymentCaseReference(updatePayment);

        ArgumentCaptor<CaseReferenceRequest> requestCaptor = ArgumentCaptor.forClass(CaseReferenceRequest.class);
        verify(payHubClient).updateCaseReference(any(), any(), requestCaptor.capture());

        CaseReferenceRequest req = requestCaptor.getValue();
        CaseReferenceRequest expectedRequest = new CaseReferenceRequest("cas-ref-9999");

        assertThat(req).usingRecursiveComparison().isEqualTo(expectedRequest);
    }

    @Test
    void should_throw_exception_if_payhub_api_update_payment_case_reference_fails() {
        UpdatePayment updatePayment = new UpdatePayment(
            "env-id-12321",
            "Divorce",
            "exp-21321",
            "cas-ref-9999"
        );

        when(s2sTokenGenerator.generate()).thenReturn("test-service");

        when(payHubClient.updateCaseReference(
            eq("test-service"), eq("exp-21321"), any(CaseReferenceRequest.class))
        ).thenThrow(mockFeignException);

        PayHubCallException exception = catchThrowableOfType(
            () -> messageHandler.updatePaymentCaseReference(updatePayment),
            PayHubCallException.class
        );

        assertThat(exception.getMessage())
            .isEqualTo(
                "Failed updating payment. "
                    + "Envelope id: env-id-12321, Exception record ref: exp-21321, New case ref: cas-ref-9999"
            );
        assertThat(exception.getCause()).isEqualTo(mockFeignException);
    }

    @Test
    void should_call_payhub_api_and_ccd_api_for_successful_payment_creation() {
        CreatePayment createPayment = new CreatePayment(
            "envelope-id-123",
            "ccd-case-ref-123",
            true,
            "po-box-123",
            "Divorce",
            "service",
            Arrays.asList(new PaymentInfo("dcn-123"))
        );

        String s2sToken = "s2sToken1";

        when(s2sTokenGenerator.generate()).thenReturn(s2sToken);

        CreatePaymentRequest request = new CreatePaymentRequest(
            "ccd-case-ref-123",
            Arrays.asList("dcn-123"),
            true,
            "test-siteId"
        );

        when(requestMapper.mapPayments(createPayment)).thenReturn(request);
        when(payHubClient.createPayment(any(), eq(request)))
            .thenReturn(ResponseEntity.of(Optional.of(new CreatePaymentResponse(Arrays.asList("dcn-123")))));

        messageHandler.handleCreatingPayment(createPayment);

        verify(payHubClient).createPayment(s2sToken, request);
        verify(ccdClient).completeAwaitingDcnProcessing(
            "ccd-case-ref-123", "service", "Divorce"
        );
    }

    @Test
    void should_handle_conflict_exception_during_payment_creation() {
        CreatePayment createPayment = new CreatePayment(
            "envelope-id-123",
            "ccd-case-ref-123",
            true,
            "po-box-123",
            "Divorce",
            "service",
            Arrays.asList(new PaymentInfo("dcn-123"))
        );

        String s2sToken = "s2sToken1";

        when(s2sTokenGenerator.generate()).thenReturn(s2sToken);

        CreatePaymentRequest request = new CreatePaymentRequest(
            "ccd-case-ref-123",
            Arrays.asList("dcn-123"),
            true,
            "test-siteId"
        );

        when(requestMapper.mapPayments(createPayment)).thenReturn(request);
        willThrow(FeignException.Conflict.class).given(payHubClient).createPayment(any(), any());

        messageHandler.handleCreatingPayment(createPayment);

        verify(payHubClient).createPayment(s2sToken, request);
        verify(ccdClient).completeAwaitingDcnProcessing(
            "ccd-case-ref-123", "service", "Divorce"
        );
    }

    @Test
    void should_throw_exception_when_payment_creation_fails_with_non_conflict_exception() {
        CreatePayment createPayment = new CreatePayment(
            "envelope-id-123",
            "ccd-case-ref-123",
            true,
            "po-box-123",
            "Divorce",
            "service",
            Arrays.asList(new PaymentInfo("dcn-123"))
        );

        String s2sToken = "s2sToken1";

        when(s2sTokenGenerator.generate()).thenReturn(s2sToken);

        CreatePaymentRequest request = new CreatePaymentRequest(
            "ccd-case-ref-123",
            Arrays.asList("dcn-123"),
            true,
            "test-siteId"
        );

        when(requestMapper.mapPayments(createPayment)).thenReturn(request);

        FeignException exception = mock(FeignException.BadRequest.class);
        doThrow(exception).when(payHubClient).createPayment(any(), any());

        PayHubCallException ex = catchThrowableOfType(
            () -> messageHandler.handleCreatingPayment(createPayment),
            PayHubCallException.class
        );

        assertThat(ex.getMessage())
            .isEqualTo("Failed creating payment. Envelope ID: envelope-id-123");
        assertThat(ex.getCause()).isEqualTo(exception);
    }
}
