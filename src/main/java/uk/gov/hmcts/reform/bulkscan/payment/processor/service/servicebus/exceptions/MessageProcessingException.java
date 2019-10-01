package uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.exceptions;

public class MessageProcessingException extends RuntimeException {

    public MessageProcessingException(String message) {
        super(message);
    }
}
