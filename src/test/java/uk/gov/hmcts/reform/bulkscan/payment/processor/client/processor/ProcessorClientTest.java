package uk.gov.hmcts.reform.bulkscan.payment.processor.client.processor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.bulkscan.payment.processor.client.processor.request.PaymentRequest;
import uk.gov.hmcts.reform.bulkscan.payment.processor.client.processor.response.PaymentStatusReponse;
import uk.gov.hmcts.reform.bulkscan.payment.processor.config.RetryConfig;
import uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.model.PaymentInfo;

import java.util.List;

import static java.util.List.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.http.HttpStatus.BAD_GATEWAY;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.GATEWAY_TIMEOUT;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@ExtendWith(MockitoExtension.class)
class ProcessorClientTest {

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private BulkScanProcessorApiProxy proxy;

    @Captor
    private ArgumentCaptor<PaymentRequest> paymentRequestArgumentCaptor;

    private ProcessorClient processorClient;


    @BeforeEach
    void setUp() {
        RetryConfig retryConfig = new RetryConfig();
        RetryTemplate retryTemplate = retryConfig.retryTemplate(5, 500);
        processorClient = new ProcessorClient(authTokenGenerator, proxy, retryTemplate);
    }

    @Test
    void should_update_payments_successfully() {
        String authToken = "authToken";
        given(authTokenGenerator.generate()).willReturn(authToken);

        List<PaymentInfo> paymentInfoList = of(
            new PaymentInfo("11234"),
            new PaymentInfo("22234"),
            new PaymentInfo("33234")
        );

        processorClient.updatePayments(paymentInfoList);

        verify(authTokenGenerator).generate();
        verify(proxy).updateStatus(eq(authToken), paymentRequestArgumentCaptor.capture());
        PaymentRequest value = paymentRequestArgumentCaptor.getValue();
        assertThat(value.payments).usingRecursiveComparison().isEqualTo(paymentInfoList);
    }

    @Test
    void should_update_payments_after_two_server_failure() {
        String authToken = "authToken";
        given(authTokenGenerator.generate()).willReturn(authToken);

        List<PaymentInfo> paymentInfoList = of(
            new PaymentInfo("11234"),
            new PaymentInfo("22234"),
            new PaymentInfo("33234")
        );

        given(proxy.updateStatus(any(), any()))
            .willThrow(
                new HttpServerErrorException(GATEWAY_TIMEOUT, GATEWAY_TIMEOUT.getReasonPhrase(), null, null, null)
            )
            .willThrow(
                new HttpServerErrorException(BAD_GATEWAY, BAD_GATEWAY.getReasonPhrase(), null, null, null)
            )
            .willReturn(new PaymentStatusReponse("success"));

        processorClient.updatePayments(paymentInfoList);

        verify(authTokenGenerator).generate();
        verify(proxy, times(3)).updateStatus(eq(authToken), paymentRequestArgumentCaptor.capture());
        PaymentRequest value = paymentRequestArgumentCaptor.getValue();
        assertThat(value.payments).usingRecursiveComparison().isEqualTo(paymentInfoList);
    }

    @Test
    void should_not_retry_payment_update_when_exception_is_client_failure() {
        String authToken = "authToken";
        given(authTokenGenerator.generate()).willReturn(authToken);

        List<PaymentInfo> paymentInfoList = of(
            new PaymentInfo("11234"),
            new PaymentInfo("22234"),
            new PaymentInfo("33234")
        );

        given(proxy.updateStatus(any(), any()))
            .willThrow(
                new HttpClientErrorException(UNAUTHORIZED, UNAUTHORIZED.getReasonPhrase(), null, null, null)
            )
            .willThrow(
                new HttpClientErrorException(BAD_REQUEST, BAD_REQUEST.getReasonPhrase(), null, null, null)
            );

        processorClient.updatePayments(paymentInfoList);

        verify(authTokenGenerator).generate();
        verify(proxy).updateStatus(eq(authToken), paymentRequestArgumentCaptor.capture());
        PaymentRequest value = paymentRequestArgumentCaptor.getValue();
        assertThat(value.payments).usingRecursiveComparison().isEqualTo(paymentInfoList);
    }
}
