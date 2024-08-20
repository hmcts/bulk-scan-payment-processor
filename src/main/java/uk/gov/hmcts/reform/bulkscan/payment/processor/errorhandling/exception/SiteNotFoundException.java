package uk.gov.hmcts.reform.bulkscan.payment.processor.errorhandling.exception;

/**
 * Exception to be thrown when the site is not found.
 */
public class SiteNotFoundException extends RuntimeException {

    /**
     * Constructor.
     * @param message The message
     */
    public SiteNotFoundException(String message) {
        super(message);
    }
}
