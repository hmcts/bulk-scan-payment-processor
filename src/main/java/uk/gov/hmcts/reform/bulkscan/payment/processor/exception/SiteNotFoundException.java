package uk.gov.hmcts.reform.bulkscan.payment.processor.exception;

public class SiteNotFoundException extends RuntimeException {

    public SiteNotFoundException(String message) {
        super(message);
    }
}
