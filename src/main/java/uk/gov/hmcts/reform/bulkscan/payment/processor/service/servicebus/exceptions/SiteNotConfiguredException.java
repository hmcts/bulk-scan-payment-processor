package uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.exceptions;

public class SiteNotConfiguredException extends RuntimeException {

    public SiteNotConfiguredException(String message) {
        super(message);
    }
}
