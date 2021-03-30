package uk.gov.hmcts.reform.bulkscan.payment.processor.client.processor;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.HttpServerErrorException;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.bulkscan.payment.processor.client.processor.request.PaymentRequest;
import uk.gov.hmcts.reform.bulkscan.payment.processor.config.IntegrationTest;
import uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.model.PaymentInfo;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.BAD_GATEWAY;
import static org.springframework.http.HttpStatus.GATEWAY_TIMEOUT;


@IntegrationTest
public class ProcessorClientTest {
    @MockBean
    private AuthTokenGenerator authTokenGenerator;

    @MockBean
    private BulkScanProcessorApiProxy proxy;

    @Autowired
    @Qualifier("RetryTemplate")
    private RetryTemplate template;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProcessorClient processorClient;

    @Test
    void should_invoke_once()  {
        List<PaymentInfo> paymentInfoList = List.of(new PaymentInfo("11234"),
                                                    new PaymentInfo("22234"), new PaymentInfo("33234"));
        PaymentRequest request = new PaymentRequest(paymentInfoList);

        when(proxy.updateStatus(any(), any())).thenReturn("Success");

        processorClient.updatePayments(paymentInfoList);
    }

    @Test
    void should_invoke_retry_twice()  {
        List<PaymentInfo> paymentInfoList = List.of(new PaymentInfo("11234"),
                                                    new PaymentInfo("22234"), new PaymentInfo("33234"));
        PaymentRequest request = new PaymentRequest(paymentInfoList);

        when(proxy.updateStatus(any(), any()))
            .thenThrow(new HttpServerErrorException(GATEWAY_TIMEOUT, GATEWAY_TIMEOUT.getReasonPhrase(),
                                                    null, null, null))
            .thenThrow(new HttpServerErrorException(BAD_GATEWAY, BAD_GATEWAY.getReasonPhrase(), null, null, null))
            .thenReturn("Success");


        processorClient.updatePayments(paymentInfoList);
    }
}
