package uk.gov.hmcts.reform.bulkscan.payment.processor.client.payhub;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import feign.FeignException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
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
    public void should_return_ok_when_everything_is_ok() {
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

        assertThat(updateResponse.getBody()).isEqualTo("{\"a\":\"1\"}");
    }

    @ParameterizedTest
    @ValueSource(ints = {400, 404, 500})
    public void should_return_FeignException_for_errors(int httpStatus) {

        String s2sToken = randomUUID().toString();
        stubWithRequestAndResponse(
            s2sToken,
            "exception_2132131",
            CASE_REF_REQUEST_JSON,
            createErrorResponse(b -> b.withStatus(httpStatus))
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
        assertThat(throwable).isInstanceOf(FeignException.class);

        // and
        FeignException exception = (FeignException) throwable;

        assertThat(exception.status()).isEqualTo(httpStatus);

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
