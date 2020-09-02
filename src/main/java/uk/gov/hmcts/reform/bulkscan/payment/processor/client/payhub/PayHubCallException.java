package uk.gov.hmcts.reform.bulkscan.payment.processor.client.payhub;

public class PayHubCallException extends RuntimeException {
    public PayHubCallException(String errorMessage, Exception cause) {
        super(errorMessage, cause);
    }
}
