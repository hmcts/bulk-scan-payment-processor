package uk.gov.hmcts.reform.bulkscan.payment.processor.client.processor;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.bulkscan.payment.processor.client.processor.request.PaymentRequest;
import uk.gov.hmcts.reform.bulkscan.payment.processor.client.processor.response.PaymentStatusReponse;
import uk.gov.hmcts.reform.bulkscan.payment.processor.config.IntegrationTest;
import uk.gov.hmcts.reform.bulkscan.payment.processor.models.PaymentInfo;
import uk.gov.hmcts.reform.bulkscan.payment.processor.util.TestUtil;

import java.io.IOException;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static java.util.List.of;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;

@IntegrationTest
public class BulkScanProcessorApiProxyTest {
    @Autowired
    private BulkScanProcessorApiProxy proxy;

    @Test
    void should_return_ok_when_payment_is_updated() throws IOException {
        List<PaymentInfo> paymentInfoList = of(
            new PaymentInfo("11234"),
            new PaymentInfo("22234"),
            new PaymentInfo("33234")
        );

        PaymentRequest request = new PaymentRequest(paymentInfoList);

        String requestJson = TestUtil.fileContentAsString("testdata.post-processor/payments_request.json");
        String responseJson = TestUtil.fileContentAsString("testdata.post-processor/payments_response.json");

        String s2sToken = randomUUID().toString();
        stubFor(
            put(urlEqualTo("/payment/status"))
                .withHeader("ServiceAuthorization", equalTo(s2sToken))
                .withRequestBody(equalToJson(requestJson))
                .willReturn(okJson(responseJson))
        );

        PaymentStatusReponse paymentStatusReponse = proxy.updateStatus(s2sToken, request);

        verify(1, putRequestedFor(urlEqualTo("/payment/status"))
            .withRequestBody(equalToJson(requestJson)));
        assertThat(paymentStatusReponse.getStatus()).isEqualTo("success");
    }
}
