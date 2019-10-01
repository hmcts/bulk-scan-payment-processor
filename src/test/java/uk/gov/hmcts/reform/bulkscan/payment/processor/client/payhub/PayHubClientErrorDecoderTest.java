package uk.gov.hmcts.reform.bulkscan.payment.processor.client.payhub;

import feign.FeignException;
import feign.Request;
import feign.Response;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import java.util.Collections;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

public class PayHubClientErrorDecoderTest {

    private static final PayHubClientErrorDecoder DECODER = new PayHubClientErrorDecoder();

    private static final String METHOD_KEY = "key";

    private static final Request REQUEST = Request.create(
        Request.HttpMethod.POST,
        "/",
        Collections.emptyMap(),
        null
    );

    @Test
    public void should_return_PayHubClientException_when_response_is_4xx_and_ignore_body() {
        // given
        Response response = createResponse(b -> b.status(HttpStatus.BAD_REQUEST.value()));

        // when
        Exception exception = DECODER.decode(METHOD_KEY, response);

        // then
        assertThat(exception).isInstanceOf(PayHubClientException.class);

        // and
        PayHubClientException payHubException = (PayHubClientException) exception;

        assertThat(payHubException.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(payHubException.getCause()).isInstanceOf(HttpClientErrorException.class);
        assertThat(((HttpClientErrorException) payHubException.getCause()).getResponseBodyAsString())
            .isEqualTo("");
    }

    @Test
    public void should_return_PayHubClientException_when_response_is_5xx_and_ignore_body() {
        // given
        Response response = createResponse(b -> b.status(HttpStatus.SERVICE_UNAVAILABLE.value()));

        // when
        Exception exception = DECODER.decode(METHOD_KEY, response);

        // then
        assertThat(exception).isInstanceOf(PayHubClientException.class);

        // and
        PayHubClientException payHubException = (PayHubClientException) exception;

        assertThat(payHubException.getStatus()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(payHubException.getCause()).isInstanceOf(HttpServerErrorException.class);
        assertThat(((HttpServerErrorException) payHubException.getCause()).getResponseBodyAsString())
            .isEqualTo("");
    }

    @Test
    public void should_return_FeignException_when_response_is_unknown_and_ignore_body() {
        // given
        Response response = createResponse(b -> b.status(HttpStatus.PERMANENT_REDIRECT.value()));
        // when
        Exception exception = DECODER.decode(METHOD_KEY, response);

        // then
        assertThat(exception).isInstanceOf(FeignException.class);
        assertThat(((FeignException) exception).status()).isEqualTo(HttpStatus.PERMANENT_REDIRECT.value());
    }

    private static Response createResponse(Function<Response.Builder, Response.Builder> p) {
        Response.Builder builder = Response.builder()
            .headers(Collections.emptyMap())
            .request(REQUEST)
            .body("ignore the body".getBytes());
        return p.apply(builder).build();
    }
}
