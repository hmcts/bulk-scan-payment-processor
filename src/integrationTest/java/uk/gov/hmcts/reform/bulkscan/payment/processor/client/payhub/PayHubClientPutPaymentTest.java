package uk.gov.hmcts.reform.bulkscan.payment.processor.client.payhub;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.bulkscan.payment.processor.client.payhub.request.CaseReferenceRequest;
import uk.gov.hmcts.reform.bulkscan.payment.processor.config.IntegrationTest;

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

@IntegrationTest
public class PayHubClientPutPaymentTest {

    private static final String CASE_REF_REQUEST_JSON = "{ \"ccd_case_number\": \"12321321\"}";

    @Autowired
    private PayHubClient client;

    @Test
    public void should_return_ok_when_everything_is_ok_ignore_response_body() {
        // given
        String s2sToken = randomUUID().toString();

        stubWithRequestAndResponse(
            s2sToken,
            "98765342",
            CASE_REF_REQUEST_JSON,
            okJson("{\"a\":\"1\"}")
        );

        // when
        ResponseEntity updateResponse =
            client.updateCaseReference(
                s2sToken,
                "98765342",
                new CaseReferenceRequest("12321321")
            );

        // then
        assertThat(updateResponse.getStatusCodeValue()).isEqualTo(200);

        //Ignore response body for now !!!
        assertThat(updateResponse.getBody()).isNull();
    }

    @ParameterizedTest
    @EnumSource(value = HttpStatus.class, names = {"BAD_REQUEST", "NOT_FOUND", "INTERNAL_SERVER_ERROR"})
    public void should_return_PayHubClientException_for_errors(HttpStatus httpStatus) {

        String s2sToken = randomUUID().toString();
        stubWithRequestAndResponse(
            s2sToken,
            "exception_2132131",
            CASE_REF_REQUEST_JSON,
            createErrorResponse(b -> b.withStatus(httpStatus.value()))
        );

        // when
        Throwable throwable = catchThrowable(
            () -> client.updateCaseReference(
                s2sToken,
                "exception_2132131",
                new CaseReferenceRequest("12321321")
            )
        );

        // then
        assertThat(throwable).isInstanceOf(PayHubClientException.class);

        // and
        PayHubClientException exception = (PayHubClientException) throwable;

        assertThat(exception.getStatus()).isEqualTo(httpStatus);

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
