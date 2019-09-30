package uk.gov.hmcts.reform.bulkscan.payment.processor.client.payhub;

import feign.Response;
import feign.codec.ErrorDecoder;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class PayHubClientErrorDecoder implements ErrorDecoder {

    private static final Logger log = LoggerFactory.getLogger(PayHubClientErrorDecoder.class);

    private static final ErrorDecoder DELEGATE = new ErrorDecoder.Default();


    @Override
    public Exception decode(String methodKey, Response response) {
        HttpStatus statusCode = HttpStatus.valueOf(response.status());
        String statusText = Optional.ofNullable(response.reason()).orElse(statusCode.getReasonPhrase());

        byte[] rawBody = new byte[0];
        String responseBody = null;

        if (response.body() != null && statusCode.is4xxClientError()) {
            try (InputStream body = response.body().asInputStream()) {
                rawBody = IOUtils.toByteArray(body);
                responseBody = new String(rawBody);
            } catch (IOException e) {
                log.error("Failed to process response body.", e);
                // don't fail and let normal exception to be returned
            }
        }

        if (statusCode.is4xxClientError()) {
            HttpClientErrorException clientException = new HttpClientErrorException(
                statusCode,
                statusText,
                rawBody,
                StandardCharsets.UTF_8
            );

            return new PayHubClientException(clientException, responseBody);
        }

        if (statusCode.is5xxServerError()) {
            HttpServerErrorException serverException = new HttpServerErrorException(
                statusCode,
                statusText,
                rawBody,
                StandardCharsets.UTF_8
            );
            return new PayHubClientException(serverException, responseBody);
        }

        return DELEGATE.decode(methodKey, response);
    }
}
