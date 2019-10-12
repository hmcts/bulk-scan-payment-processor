package uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.exceptions;

public class UnrecoverableMessageProcessingException extends RuntimeException {

    public UnrecoverableMessageProcessingException(String message) {
        super(message);
    }
}
