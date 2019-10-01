package uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.exceptions;

public class InvalidMessageException extends RuntimeException {
    public InvalidMessageException(Throwable cause) {
        super(cause);
    }
}
