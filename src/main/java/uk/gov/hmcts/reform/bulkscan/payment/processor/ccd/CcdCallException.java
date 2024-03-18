package uk.gov.hmcts.reform.bulkscan.payment.processor.ccd;

/**
 * Exception to be thrown when there is an error calling CCD API.
 */
public class CcdCallException extends RuntimeException {

    /**
     * Constructor for the CcdCallException.
     * @param errorMessage The error message
     * @param cause The cause
     */
    public CcdCallException(String errorMessage, Exception cause) {
        super(errorMessage, cause);
    }
}
