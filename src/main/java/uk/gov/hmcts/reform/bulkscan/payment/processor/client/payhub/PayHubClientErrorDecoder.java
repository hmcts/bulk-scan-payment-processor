package uk.gov.hmcts.reform.bulkscan.payment.processor.client.payhub;

import feign.Response;
import feign.codec.ErrorDecoder;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import java.util.Optional;

public class PayHubClientErrorDecoder implements ErrorDecoder {

    private static final ErrorDecoder DELEGATE = new ErrorDecoder.Default();


    @Override
    public Exception decode(String methodKey, Response response) {
        HttpStatus statusCode = HttpStatus.valueOf(response.status());
        String statusText = Optional.ofNullable(response.reason()).orElse(statusCode.getReasonPhrase());

        if (statusCode.is4xxClientError()) {
            HttpClientErrorException clientException = new HttpClientErrorException(
                statusCode,
                statusText
            );

            return new PayHubClientException(clientException);
        }

        if (statusCode.is5xxServerError()) {
            HttpServerErrorException serverException = new HttpServerErrorException(
                statusCode,
                statusText
            );
            return new PayHubClientException(serverException);
        }

        return DELEGATE.decode(methodKey, response);
    }
}
