package uk.gov.hmcts.reform.bulkscan.payment.processor.client.processor;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.bulkscan.payment.processor.client.processor.request.PaymentRequest;
import uk.gov.hmcts.reform.bulkscan.payment.processor.config.IntegrationTest;
import uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.model.PaymentInfo;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static java.util.List.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.BAD_GATEWAY;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.GATEWAY_TIMEOUT;


@IntegrationTest
public class ProcessorClientTest {
    @MockBean
    private AuthTokenGenerator authTokenGenerator;

    @MockBean
    private BulkScanProcessorApiProxy proxy;

    @Autowired
    private ProcessorClient processorClient;

    @Test
    void should_invoke_once() throws ExecutionException, InterruptedException {
        List<PaymentInfo> paymentInfoList = of(
            new PaymentInfo("11234"),
            new PaymentInfo("22234"),
            new PaymentInfo("33234")
        );

        when(authTokenGenerator.generate()).thenReturn("authToken");
        when(proxy.updateStatus(any(), any())).thenReturn("Success");

        Future<Boolean> booleanFuture = processorClient.updatePayments(paymentInfoList);
        assertThat(booleanFuture.get()).isTrue();

        verify(proxy).updateStatus(isA(String.class),isA(PaymentRequest.class));
    }

    @Test
    void should_invoke_retry_thrice() throws ExecutionException, InterruptedException {
        List<PaymentInfo> paymentInfoList = of(
            new PaymentInfo("11234"),
            new PaymentInfo("22234"),
            new PaymentInfo("33234")
        );

        when(authTokenGenerator.generate()).thenReturn("authToken");

        when(proxy.updateStatus(any(), any()))
            .thenThrow(new HttpServerErrorException(GATEWAY_TIMEOUT, GATEWAY_TIMEOUT.getReasonPhrase(),
                                                    null, null, null))
            .thenThrow(new HttpServerErrorException(BAD_GATEWAY, BAD_GATEWAY.getReasonPhrase(), null, null, null))
            .thenReturn("Success");

        CompletableFuture<Boolean> booleanFuture  = processorClient.updatePayments(paymentInfoList);
        assertThat(booleanFuture.get()).isTrue();
        assertThat(booleanFuture.isDone()).isTrue();
        verify(proxy, times(3)).updateStatus(isA(String.class),isA(PaymentRequest.class));
    }

    @Test
    void should_invoke_fail_after_five() throws ExecutionException, InterruptedException {
        List<PaymentInfo> paymentInfoList = of(
            new PaymentInfo("11234"),
            new PaymentInfo("22234"),
            new PaymentInfo("33234")
        );

        when(authTokenGenerator.generate()).thenReturn("authToken");

        when(proxy.updateStatus(any(), any()))
            .thenThrow(new HttpServerErrorException(GATEWAY_TIMEOUT, GATEWAY_TIMEOUT.getReasonPhrase(),
                                                    null, null, null));
        CompletableFuture<Boolean> booleanFuture =  processorClient.updatePayments(paymentInfoList);

        assertThatThrownBy(() -> booleanFuture.get())
            .isInstanceOf(ExecutionException.class)
            .hasMessage("org.springframework.web.client.HttpServerErrorException: 504 Gateway Timeout");

        assertThat(booleanFuture.isCompletedExceptionally()).isTrue();
        verify(proxy, times(5)).updateStatus(isA(String.class),isA(PaymentRequest.class));
    }

    @Test
    void should_not_retry_client_exception() throws ExecutionException, InterruptedException {
        List<PaymentInfo> paymentInfoList = of(
            new PaymentInfo("11234"),
            new PaymentInfo("22234"),
            new PaymentInfo("33234")
        );

        when(authTokenGenerator.generate()).thenReturn("authToken");

        when(proxy.updateStatus(any(), any()))
            .thenThrow(new HttpClientErrorException(BAD_REQUEST, BAD_REQUEST.getReasonPhrase(),
                                                    null, null, null));

        CompletableFuture<Boolean> booleanFuture =  processorClient.updatePayments(paymentInfoList);

        assertThatThrownBy(() -> booleanFuture.get())
            .isInstanceOf(ExecutionException.class)
            .hasMessage("org.springframework.web.client.HttpClientErrorException: 400 Bad Request");

        assertThat(booleanFuture.isCompletedExceptionally()).isTrue();
        verify(proxy, times(1)).updateStatus(isA(String.class),isA(PaymentRequest.class));
    }

}
