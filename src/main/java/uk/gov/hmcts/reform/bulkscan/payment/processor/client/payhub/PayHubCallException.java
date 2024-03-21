package uk.gov.hmcts.reform.bulkscan.payment.processor.client.payhub;

/**
 * Exception to be thrown when there is an error calling PayHub API.
 */
public class PayHubCallException extends RuntimeException {

    /**
     * Constructor for the PayHubCallException.
     * @param errorMessage The error message
     * @param cause The cause
     */
    public PayHubCallException(String errorMessage, Exception cause) {
        super(errorMessage, cause);
    }
}
