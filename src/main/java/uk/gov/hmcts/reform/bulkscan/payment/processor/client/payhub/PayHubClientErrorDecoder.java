package uk.gov.hmcts.reform.bulkscan.payment.processor.client.payhub;

import com.google.common.io.CharStreams;
import feign.Response;
import feign.codec.ErrorDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import java.io.IOException;
import java.util.Optional;

public class PayHubClientErrorDecoder implements ErrorDecoder {
    private static final Logger log = LoggerFactory.getLogger(PayHubClientErrorDecoder.class);


    private static final ErrorDecoder DELEGATE = new ErrorDecoder.Default();


    @Override
    public Exception decode(String methodKey, Response response) {
        HttpStatus statusCode = HttpStatus.valueOf(response.status());
        String statusText = Optional.ofNullable(response.reason()).orElse(statusCode.getReasonPhrase());
        try {
            log.error("statusText: {}, body: {}", statusText, CharStreams.toString(response.body().asReader()));
        } catch(IOException ex) {
            log.error("IOEx: " + ex);
        }

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
