package uk.gov.hmcts.reform.bulkscan.payment.processor.client.processor;

import feign.Response;
import feign.codec.ErrorDecoder;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Optional;

public class CustomFeignErrorDecoder implements ErrorDecoder {
    private static final Logger logger = LoggerFactory.getLogger(CustomFeignErrorDecoder.class);
    private ErrorDecoder delegate = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        HttpHeaders responseHeaders = new HttpHeaders();
        response.headers()
            .forEach((key, value) -> responseHeaders.put(key, new ArrayList<>(value)));

        HttpStatus statusCode = HttpStatus.valueOf(response.status());
        String statusText = Optional.ofNullable(response.reason()).orElse(statusCode.getReasonPhrase());

        byte[] responseBody = null;

        if (response.body() != null && response.body().length() != null) {
            try (InputStream body = response.body().asInputStream()) {
                responseBody = IOUtils.toByteArray(body);
            } catch (IOException e) {
                throw new RuntimeException("Failed to process response body.", e);
            }
        }

        if (statusCode.is4xxClientError()) {
            return new HttpClientErrorException(statusCode, statusText, responseHeaders, responseBody, null);
        }

        if (statusCode.is5xxServerError()) {
            return new HttpServerErrorException(statusCode, statusText, responseHeaders, responseBody, null);
        }

        return delegate.decode(methodKey, response);
    }
}
