package uk.gov.hmcts.reform.bulkscan.payment.processor.client.payhub;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.bulkscan.payment.processor.client.payhub.request.CaseReferenceRequest;
import uk.gov.hmcts.reform.bulkscan.payment.processor.config.IntegrationTest;

import java.io.IOException;
import java.util.function.Function;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static uk.gov.hmcts.reform.bulkscan.payment.processor.util.TestUtil.fileContentAsString;

@IntegrationTest
public class PayHubClientPutPaymentTest {

    private static final String CASE_REF_REQUEST_JSON = "testdata/put-payments/case-reference-request.json";

    @Autowired
    private PayHubClient client;

    @Test
    public void should_return_ok_when_everything_is_ok_ignore_response_body() throws IOException {
        // given
        String s2sToken = randomUUID().toString();

        stubWithRequestAndResponse(
            s2sToken,
            "98765342",
            fileContentAsString(CASE_REF_REQUEST_JSON),
            okJson("{\"a\":\"1\"}")
        );

        // when
        ResponseEntity updateResponse =
            client.putPayments(
                s2sToken,
                "98765342",
                new CaseReferenceRequest("12321321")
            );

        // then
        assertThat(updateResponse.getStatusCodeValue()).isEqualTo(200);

        //Ignore response body for now !!!
        assertThat(updateResponse.getBody()).isNull();
    }

    @Test
    public void should_return_PayHubClientException_for_badRequest() throws IOException {
        // given
        String message = "error occurred";
        String s2sToken = randomUUID().toString();

        stubWithRequestAndResponse(
            s2sToken,
            "exception_2132131",
            fileContentAsString(CASE_REF_REQUEST_JSON),
            createErrorResponse(bul -> bul.withStatus(400).withBody(message.getBytes()))
        );

        // when
        Throwable throwable = catchThrowable(() ->
                                                 client.putPayments(
                                                     s2sToken,
                                                     "exception_2132131",
                                                     new CaseReferenceRequest("12321321")
                                                 )
        );

        // then
        assertThat(throwable).isInstanceOf(PayHubClientException.class);

        // and
        PayHubClientException exception = (PayHubClientException) throwable;

        assertThat(exception.getStatus()).isEqualTo(BAD_REQUEST);
    }

    @Test
    public void should_return_PayHubClientException_for_notFound() throws IOException {
        // given
        String s2sToken = randomUUID().toString();

        stubWithRequestAndResponse(
            s2sToken,
            "exception_2132131",
            fileContentAsString(CASE_REF_REQUEST_JSON),
            createErrorResponse(bul -> bul.withStatus(404))
        );

        // when
        Throwable throwable = catchThrowable(() ->
                                                 client.putPayments(
                                                     s2sToken,
                                                     "exception_2132131",
                                                     new CaseReferenceRequest("12321321")
                                                 )
        );

        // then
        assertThat(throwable).isInstanceOf(PayHubClientException.class);

        // and
        PayHubClientException exception = (PayHubClientException) throwable;

        assertThat(exception.getStatus()).isEqualTo(NOT_FOUND);
    }

    @Test
    public void should_return_PayHubClientException_for_serverError()
        throws IOException {
        // given
        String s2sToken = randomUUID().toString();
        stubWithRequestAndResponse(
            s2sToken,
            "exception_2132131",
            fileContentAsString(CASE_REF_REQUEST_JSON),
            createErrorResponse(bul -> bul.withStatus(500))
        );

        // when
        Throwable throwable = catchThrowable(() ->
                                                 client.putPayments(
                                                     s2sToken,
                                                     "exception_2132131",
                                                     new CaseReferenceRequest("12321321")
                                                 )
        );

        // then
        assertThat(throwable).isInstanceOf(PayHubClientException.class);

        // and
        PayHubClientException exception = (PayHubClientException) throwable;

        assertThat(exception.getStatus()).isEqualTo(INTERNAL_SERVER_ERROR);
    }

    private ResponseDefinitionBuilder createErrorResponse(
        Function<ResponseDefinitionBuilder, ResponseDefinitionBuilder> func
    ) {
        return func.apply(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));
    }

    private static void stubWithRequestAndResponse(
        String s2sToken,
        String queryParam,
        String requestStr,
        ResponseDefinitionBuilder builder
    ) {
        stubFor(
            put(urlEqualTo("/bulk-scan-payments?exception_reference=" + queryParam))
                .withHeader("ServiceAuthorization", equalTo(s2sToken))
                .withRequestBody(equalToJson(requestStr))
                .willReturn(builder)
        );
    }
}
