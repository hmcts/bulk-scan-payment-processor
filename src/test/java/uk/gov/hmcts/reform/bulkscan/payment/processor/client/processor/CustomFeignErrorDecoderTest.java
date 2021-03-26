package uk.gov.hmcts.reform.bulkscan.payment.processor.client.processor;

import feign.FeignException;
import feign.Request;
import feign.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import java.io.IOException;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;


@ExtendWith(MockitoExtension.class)
class CustomFeignErrorDecoderTest {

    private static final CustomFeignErrorDecoder DECODER = new CustomFeignErrorDecoder();

    private static final Request REQUEST = Request.create(
        Request.HttpMethod.GET,
        "localhost",
        Collections.emptyMap(),
        Request.Body.create(new byte[0]),
        null
    );

    @DisplayName("Should parse response and return Client specific exception")
    @Test
    public void should_throw_Client_Exception() {
        Response response = Response.builder()
            .request(REQUEST)
            .headers(Collections.singletonMap("AcceptTest", Collections.singletonList("Yes")))
            .status(HttpStatus.NOT_FOUND.value())
            .reason("Could not find")
            .body("some body".getBytes())
            .build();

        assertThat(decode(response))
            .isInstanceOf(HttpClientErrorException.class)
            .hasMessage(HttpStatus.NOT_FOUND.value() + " Could not find");
    }

    @DisplayName("Should parse response and return Server specific exception")
    @Test
    public void should_throw_Server_Exception() {
        Response response = Response.builder()
            .request(REQUEST)
            .headers(Collections.emptyMap())
            .status(INTERNAL_SERVER_ERROR.value())
            .body("some body".getBytes())
            .build();

        assertThat(decode(response))
            .isInstanceOf(HttpServerErrorException.class)
            .hasMessage(INTERNAL_SERVER_ERROR.value() + " " + INTERNAL_SERVER_ERROR.getReasonPhrase());
    }

    @DisplayName("Should fail to parse body and throw RuntimeException instead")
    @Test
    public void should_throw_Failing_Body_Parsing() throws IOException {
        Response.Body body = mock(Response.Body.class);
        Response response = Response.builder()
            .request(REQUEST)
            .headers(Collections.emptyMap())
            .status(HttpStatus.BAD_REQUEST.value())
            .reason("bad")
            .body(body)
            .build();

        when(body.asInputStream()).thenThrow(IOException.class);

        assertThatCode(() -> decode(response))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Failed to process response body.")
            .hasCauseInstanceOf(IOException.class);
    }

    @DisplayName("Should decode valid response in case somehow it got in the process")
    @Test
    public void should_throw_FeignException() {
        Response response = Response.builder()
            .request(REQUEST)
            .headers(Collections.emptyMap())
            .status(HttpStatus.TEMPORARY_REDIRECT.value())
            .reason("nope")
            .body("grumps".getBytes())
            .build();

        assertThat(decode(response))
            .isInstanceOf(FeignException.class)
            .hasMessage("[" + HttpStatus.TEMPORARY_REDIRECT.value()
                            + " nope] during [GET] to [localhost] [methodKey]: [grumps]");
    }

    @DisplayName("Should decode when response body is not present")
    @Test
    public void testResponseBodyIsNull() {
        Response response = Response.builder()
            .request(REQUEST)
            .headers(Collections.emptyMap())
            .status(HttpStatus.TEMPORARY_REDIRECT.value())
            .reason("nope")
            .build();

        assertThat(decode(response))
            .isInstanceOf(FeignException.class)
            .hasMessage("[" + HttpStatus.TEMPORARY_REDIRECT.value()
                            + " nope] during [GET] to [localhost] [methodKey]: []");
    }

    private Exception decode(Response response) {
        return DECODER.decode("methodKey", response);
    }
}
