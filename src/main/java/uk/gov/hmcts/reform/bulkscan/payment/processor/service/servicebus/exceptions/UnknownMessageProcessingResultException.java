package uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.exceptions;

public class UnknownMessageProcessingResultException extends RuntimeException {

    public UnknownMessageProcessingResultException(String message) {
        super(message);
    }
}
