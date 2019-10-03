package uk.gov.hmcts.reform.bulkscan.payment.processor.exception;

public class SiteConfigurationException extends RuntimeException {

    public SiteConfigurationException(String message) {
        super(message);
    }
}
