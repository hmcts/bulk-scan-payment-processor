package uk.gov.hmcts.reform.bulkscan.payment.processor.client.payhub;

import com.fasterxml.jackson.core.JsonProcessingException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.bulkscan.payment.processor.client.payhub.request.PaymentRequest;
import uk.gov.hmcts.reform.bulkscan.payment.processor.client.payhub.response.PaymentResult;
import uk.gov.hmcts.reform.bulkscan.payment.processor.config.IntegrationTest;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.badRequest;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.serverError;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.google.common.net.HttpHeaders.CONTENT_TYPE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@AutoConfigureWireMock
@IntegrationTest
@RunWith(SpringRunner.class)
public class PayHubClientTest {


    @Autowired
    private PayHubClient client;

    @Autowired
    private ObjectMapper mapper;

    @Test
    public void should_return_Ok_when_everything_is_ok_with_request() throws JsonProcessingException {
        // given
        PaymentResult response = new PaymentResult(ImmutableList.of("xxxyyyzzz", "zzzyyyxxx"));
        stubWithResponse(okJson(mapper.writeValueAsString(response)),mapper.writeValueAsString(getPaymentRequest()));

        // when
        ResponseEntity<PaymentResult> paymentResponse = client.postPayments(getPaymentRequest());

        // then
        assertThat(paymentResponse.getStatusCodeValue()).isEqualTo(200);

        assertThat(paymentResponse.getBody()).isEqualToComparingFieldByField(response);
    }


    @Test
    public void should_return_Created_when_everything_is_ok_with_request() throws JsonProcessingException {
        // given
        PaymentResult response = new PaymentResult(ImmutableList.of("xxxyyyzzz"));
        stubWithResponse(aResponse()
                             .withStatus(201)
                             .withHeader(CONTENT_TYPE, "application/json")
                             .withBody(mapper.writeValueAsString(response)));

        // when
        ResponseEntity<PaymentResult> paymentResponse = client.postPayments(getPaymentRequest());

        // then
        assertThat(paymentResponse.getStatusCodeValue()).isEqualTo(201);
        assertThat(paymentResponse.getBody()).isEqualToComparingFieldByField(response);
    }


    @Test
    public void should_return_PayHubClientException_for_badRequest()
        throws JsonProcessingException {
        // given
        String message = "error occurred";
        stubWithResponse(getBadRequest(message));

        // when
        Throwable throwable = catchThrowable(() -> client.postPayments(getPaymentRequest()));

        // then
        assertThat(throwable).isInstanceOf(PayHubClientException.class);

        // and
        PayHubClientException exception = (PayHubClientException) throwable;

        assertThat(exception.getStatus()).isEqualTo(BAD_REQUEST);
        assertThat(exception.getMessage()).isEqualTo(message);
    }

    @Test
    public void should_return_PayHubClientException_for_serverError()
        throws JsonProcessingException {
        // given
        stubWithResponse(getServerErrorRequest());

        // when
        Throwable throwable = catchThrowable(() -> client.postPayments(getPaymentRequest()));

        // then
        assertThat(throwable).isInstanceOf(PayHubClientException.class);

        // and
        PayHubClientException exception = (PayHubClientException) throwable;

        assertThat(exception.getStatus()).isEqualTo(INTERNAL_SERVER_ERROR);
        assertThat(exception.getMessage()).isEqualTo(null);
    }


    private ResponseDefinitionBuilder getBadRequest(String bodyMessage) throws JsonProcessingException {
        return badRequest()
            .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .withBody(get4xxResponseBody(bodyMessage));
    }


    private ResponseDefinitionBuilder getServerErrorRequest() throws JsonProcessingException {
        return serverError()
            .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
    }

    private static byte[] get4xxResponseBody(String message) {
        return message == null ? null : message.getBytes();
    }

    private static void stubWithResponse(ResponseDefinitionBuilder builder) {
        stubFor(post("/bulk-scan-payments").willReturn(builder));
    }

    private static void stubWithResponse(ResponseDefinitionBuilder builder,String requestStr) {
        stubFor(post("/bulk-scan-payments").withRequestBody(equalToJson(requestStr)).willReturn(builder));
    }

    private static PaymentRequest getPaymentRequest() {
        return new PaymentRequest(
            "3213213123",
            ImmutableList.of("xxxyyyzzz", "zzzyyyxxx"),
            true,
            "A231"
        );
    }

}
