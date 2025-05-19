package uk.gov.hmcts.reform.bulkscan.payment.processor.client.processor;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.bulkscan.payment.processor.client.processor.request.PaymentRequest;
import uk.gov.hmcts.reform.bulkscan.payment.processor.client.processor.response.PaymentStatusReponse;
import uk.gov.hmcts.reform.bulkscan.payment.processor.config.IntegrationTest;
import uk.gov.hmcts.reform.bulkscan.payment.processor.models.PaymentInfo;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static java.util.List.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.http.HttpStatus.BAD_GATEWAY;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.GATEWAY_TIMEOUT;

@IntegrationTest
public class ProcessorClientTest {
    @MockitoBean
    private AuthTokenGenerator authTokenGenerator;

    @MockitoBean
    private BulkScanProcessorApiProxy proxy;

    @Autowired
    private ProcessorClient processorClient;

    @Test
    void should_sucessfully_update_payments() throws ExecutionException, InterruptedException {
        List<PaymentInfo> paymentInfoList = of(
            new PaymentInfo("11234"),
            new PaymentInfo("22234"),
            new PaymentInfo("33234")
        );

        given(authTokenGenerator.generate()).willReturn("authToken");
        given(proxy.updateStatus(any(), any())).willReturn(new PaymentStatusReponse("success"));

        Future<Boolean> paymentUpdated = processorClient.updatePayments(paymentInfoList);
        assertThat(paymentUpdated.get()).isTrue();

        verify(proxy).updateStatus(isA(String.class),isA(PaymentRequest.class));
    }

    @Test
    void should_update_payments_after_two_server_failure() throws ExecutionException, InterruptedException {
        List<PaymentInfo> paymentInfoList = of(
            new PaymentInfo("11234"),
            new PaymentInfo("22234"),
            new PaymentInfo("33234")
        );

        given(authTokenGenerator.generate()).willReturn("authToken");

        given(proxy.updateStatus(any(), any()))
            .willThrow(
                new HttpServerErrorException(GATEWAY_TIMEOUT, GATEWAY_TIMEOUT.getReasonPhrase(), null, null, null)
            )
            .willThrow(
                new HttpServerErrorException(BAD_GATEWAY, BAD_GATEWAY.getReasonPhrase(), null, null, null)
            )
            .willReturn(new PaymentStatusReponse("success"));

        CompletableFuture<Boolean> paymentUpdated  = processorClient.updatePayments(paymentInfoList);
        assertThat(paymentUpdated.get()).isTrue();
        assertThat(paymentUpdated.isDone()).isTrue();
        verify(proxy, times(3)).updateStatus(isA(String.class),isA(PaymentRequest.class));
    }

    @Test
    void should_fail_after_five_retries_when_exception_is_server_failure() {
        List<PaymentInfo> paymentInfoList = of(
            new PaymentInfo("11234"),
            new PaymentInfo("22234"),
            new PaymentInfo("33234")
        );

        given(authTokenGenerator.generate()).willReturn("authToken");

        given(proxy.updateStatus(any(), any()))
            .willThrow(
                new HttpServerErrorException(GATEWAY_TIMEOUT, GATEWAY_TIMEOUT.getReasonPhrase(), null, null, null)
            );

        CompletableFuture<Boolean> paymentUpdated =  processorClient.updatePayments(paymentInfoList);

        assertThatThrownBy(paymentUpdated::get)
            .isInstanceOf(ExecutionException.class)
            .hasMessage("org.springframework.web.client.HttpServerErrorException: 504 Gateway Timeout");

        assertThat(paymentUpdated.isCompletedExceptionally()).isTrue();
        verify(proxy, times(5)).updateStatus(isA(String.class),isA(PaymentRequest.class));
    }

    @Test
    void should_not_retry_when_exception_is_client_failure() {
        List<PaymentInfo> paymentInfoList = of(
            new PaymentInfo("11234"),
            new PaymentInfo("22234"),
            new PaymentInfo("33234")
        );

        given(authTokenGenerator.generate()).willReturn("authToken");

        given(proxy.updateStatus(any(), any()))
            .willThrow(
                new HttpClientErrorException(BAD_REQUEST, BAD_REQUEST.getReasonPhrase(), null, null, null)
            );

        CompletableFuture<Boolean> paymentUpdated =  processorClient.updatePayments(paymentInfoList);

        assertThatThrownBy(paymentUpdated::get)
            .isInstanceOf(ExecutionException.class)
            .hasMessage("org.springframework.web.client.HttpClientErrorException: 400 Bad Request");

        assertThat(paymentUpdated.isCompletedExceptionally()).isTrue();
        verify(proxy, times(1)).updateStatus(isA(String.class),isA(PaymentRequest.class));
    }
}
