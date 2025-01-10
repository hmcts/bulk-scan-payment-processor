package uk.gov.hmcts.reform.bulkscan.payment.processor.service.exceptions;

/**
 * InvalidMessageException.
 */
public class InvalidMessageException extends RuntimeException {

    /**
     * Constructor.
     * @param message The message
     */
    public InvalidMessageException(String message) {
        super(message);
    }

    /**
     * Constructor.
     * @param cause The cause
     */
    public InvalidMessageException(Throwable cause) {
        super(cause);
    }
}
