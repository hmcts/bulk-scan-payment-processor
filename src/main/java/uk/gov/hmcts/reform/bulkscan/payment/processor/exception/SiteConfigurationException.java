package uk.gov.hmcts.reform.bulkscan.payment.processor.exception;

/**
 * Exception to be thrown when the site configuration is invalid.
 */
public class SiteConfigurationException extends RuntimeException {

    /**
     * Constructor.
     * @param message The message
     */
    public SiteConfigurationException(String message) {
        super(message);
    }
}
