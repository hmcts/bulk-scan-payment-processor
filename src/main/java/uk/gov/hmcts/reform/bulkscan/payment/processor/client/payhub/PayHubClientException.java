package uk.gov.hmcts.reform.bulkscan.payment.processor.client.payhub;

import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;

public class PayHubClientException extends RuntimeException {

    private final HttpStatus status;

    private final String message;

    public PayHubClientException(HttpStatusCodeException cause, String message) {
        super(cause);

        this.status = cause.getStatusCode();
        this.message = message;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public String getResponseRawBody() {
        return ((HttpStatusCodeException) getCause()).getResponseBodyAsString();
    }
}
