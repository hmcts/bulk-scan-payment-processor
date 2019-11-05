package uk.gov.hmcts.reform.bulkscan.payment.processor.client.payhub;

import feign.FeignException;
import org.springframework.http.HttpStatus;

public class PayHubClientException extends RuntimeException {

    private static final long serialVersionUID = 7808950893080049434L;

    private final HttpStatus status;

    public PayHubClientException(FeignException cause) {
        super(cause);
        this.status = HttpStatus.resolve(cause.status());
    }

    public HttpStatus getStatus() {
        return status;
    }
}
