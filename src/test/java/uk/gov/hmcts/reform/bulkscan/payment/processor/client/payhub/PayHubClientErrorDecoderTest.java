package uk.gov.hmcts.reform.bulkscan.payment.processor.client.payhub;

import feign.FeignException;
import feign.Request;
import feign.Response;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static feign.Request.HttpMethod.POST;
import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;

public class PayHubClientErrorDecoderTest {

    private static final PayHubClientErrorDecoder DECODER = new PayHubClientErrorDecoder();

    @Test
    public void should_return_PayHubClientException_with_status_based_on_body_and_feign_exception_as_cause() {
        // given
        Response response = Response
            .builder()
            .headers(emptyMap())
            .request(Request.create(POST, "/", emptyMap(), null))
            .status(400)
            .build();

        String methodKey = "some_method_key";

        // when
        Exception exception = DECODER.decode(methodKey, response);

        // then
        assertThat(exception).isInstanceOf(PayHubClientException.class);

        // and
        PayHubClientException payHubException = (PayHubClientException) exception;

        assertThat(payHubException.getStatus()).isEqualTo(HttpStatus.resolve(response.status()));
        assertThat(payHubException.getCause()).isInstanceOf(FeignException.class);
        assertThat(payHubException.getMessage()).contains(methodKey);
        assertThat(payHubException.getMessage()).contains(String.valueOf(response.status()));
    }
}
