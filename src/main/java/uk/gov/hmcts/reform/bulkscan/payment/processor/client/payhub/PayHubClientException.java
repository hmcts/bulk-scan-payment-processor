package uk.gov.hmcts.reform.bulkscan.payment.processor.client.payhub;

import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;

public class PayHubClientException extends RuntimeException {

    private static final long serialVersionUID = 7808950893080049434L;

    private final HttpStatus status;

    public PayHubClientException(HttpStatusCodeException cause) {
        super(cause);
        this.status = cause.getStatusCode();
    }

    public HttpStatus getStatus() {
        return status;
    }
}
