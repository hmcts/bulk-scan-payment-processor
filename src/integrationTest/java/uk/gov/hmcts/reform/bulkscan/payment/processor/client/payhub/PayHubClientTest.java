package uk.gov.hmcts.reform.bulkscan.payment.processor.client.payhub;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.bulkscan.payment.processor.client.payhub.request.CreatePaymentRequest;
import uk.gov.hmcts.reform.bulkscan.payment.processor.client.payhub.response.CreatePaymentResponse;
import uk.gov.hmcts.reform.bulkscan.payment.processor.config.IntegrationTest;

import java.io.IOException;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.badRequest;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.serverError;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.google.common.net.HttpHeaders.CONTENT_TYPE;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static uk.gov.hmcts.reform.bulkscan.payment.processor.util.TestUtil.fileContentAsString;

@IntegrationTest
public class PayHubClientTest {

    private static final String PAYMENT_REQUEST_JSON = "testdata/post-payments/payment-request.json";

    @Autowired
    private PayHubClient client;

    @Test
    public void should_return_Ok_when_everything_is_ok_with_request() throws IOException {
        // given
        String s2sToken = randomUUID().toString();

        stubWithRequestAndResponse(
            s2sToken,
            fileContentAsString(PAYMENT_REQUEST_JSON),
            okJson(fileContentAsString("testdata/post-payments/payment-result-1.json"))
        );

        CreatePaymentResponse response = new CreatePaymentResponse(ImmutableList.of("123444", "666666"));

        // when
        ResponseEntity<CreatePaymentResponse> paymentResponse = client.createPayment(s2sToken, getPaymentRequest());

        // then
        assertThat(paymentResponse.getStatusCodeValue()).isEqualTo(200);

        assertThat(paymentResponse.getBody()).isEqualToComparingFieldByField(response);
    }

    @Test
    public void should_return_Created_when_everything_is_ok_with_request() throws IOException {
        // given
        String s2sToken = randomUUID().toString();

        stubWithRequestAndResponse(
            s2sToken,
            fileContentAsString(PAYMENT_REQUEST_JSON),
            aResponse()
                .withStatus(201)
                .withHeader(CONTENT_TYPE, "application/json")
                .withBody(fileContentAsString("testdata/post-payments/payment-result-2.json"))
        );

        CreatePaymentResponse response = new CreatePaymentResponse(ImmutableList.of("DCN-4343"));

        // when
        ResponseEntity<CreatePaymentResponse> paymentResponse = client.createPayment(s2sToken, getPaymentRequest());

        // then
        assertThat(paymentResponse.getStatusCodeValue()).isEqualTo(201);
        assertThat(paymentResponse.getBody()).isEqualToComparingFieldByField(response);
    }


    @Test
    public void should_return_PayHubClientException_for_badRequest() throws IOException {
        // given
        String message = "error occurred";
        String s2sToken = randomUUID().toString();

        stubWithRequestAndResponse(
            s2sToken,
            fileContentAsString(PAYMENT_REQUEST_JSON),
            getBadRequest(message)
        );

        // when
        Throwable throwable = catchThrowable(() -> client.createPayment(s2sToken, getPaymentRequest()));

        // then
        assertThat(throwable).isInstanceOf(PayHubClientException.class);

        // and
        PayHubClientException exception = (PayHubClientException) throwable;

        assertThat(exception.getStatus()).isEqualTo(BAD_REQUEST);
    }


    @Test
    public void should_return_PayHubClientException_for_conflict() throws IOException {
        // given
        String s2sToken = randomUUID().toString();

        stubWithRequestAndResponse(
            s2sToken,
            fileContentAsString(PAYMENT_REQUEST_JSON),
            aResponse().withStatus(409)
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        );

        // when
        Throwable throwable = catchThrowable(() -> client.createPayment(s2sToken, getPaymentRequest()));

        // then
        assertThat(throwable).isInstanceOf(PayHubClientException.class);

        // and
        PayHubClientException exception = (PayHubClientException) throwable;

        assertThat(exception.getStatus()).isEqualTo(CONFLICT);
    }

    @Test
    public void should_return_PayHubClientException_for_serverError()
        throws IOException {
        // given
        String s2sToken = randomUUID().toString();
        stubWithRequestAndResponse(
            s2sToken,
            fileContentAsString(PAYMENT_REQUEST_JSON),
            getServerErrorRequest()
        );

        // when
        Throwable throwable = catchThrowable(() -> client.createPayment(s2sToken, getPaymentRequest()));

        // then
        assertThat(throwable).isInstanceOf(PayHubClientException.class);

        // and
        PayHubClientException exception = (PayHubClientException) throwable;

        assertThat(exception.getStatus()).isEqualTo(INTERNAL_SERVER_ERROR);
    }


    private ResponseDefinitionBuilder getBadRequest(String bodyMessage) {
        return badRequest()
            .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .withBody(get4xxResponseBody(bodyMessage));
    }

    private ResponseDefinitionBuilder getServerErrorRequest() {
        return serverError()
            .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
    }

    private static byte[] get4xxResponseBody(String message) {
        return message == null ? null : message.getBytes();
    }

    private static void stubWithRequestAndResponse(
        String s2sToken,
        String requestStr,
        ResponseDefinitionBuilder builder
    ) {
        stubFor(
            post("/bulk-scan-payments")
                .withHeader("ServiceAuthorization", equalTo(s2sToken))
                .withRequestBody(equalToJson(requestStr)).willReturn(builder)
        );
    }

    private static CreatePaymentRequest getPaymentRequest() {
        return new CreatePaymentRequest(
            "3213213123",
            ImmutableList.of("xxxyyyzzz", "zzzyyyxxx"),
            true,
            "A231"
        );
    }

}
