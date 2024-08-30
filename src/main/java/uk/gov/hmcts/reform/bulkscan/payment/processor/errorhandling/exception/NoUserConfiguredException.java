package uk.gov.hmcts.reform.bulkscan.payment.processor.errorhandling.exception;

/**
 * NoUserConfiguredException.
 */
public class NoUserConfiguredException extends RuntimeException {

    /**
     * Constructor.
     * @param jurisdiction The jurisdiction
     */
    public NoUserConfiguredException(String jurisdiction) {
        super("No user configured for jurisdiction: " + jurisdiction);
    }
}
