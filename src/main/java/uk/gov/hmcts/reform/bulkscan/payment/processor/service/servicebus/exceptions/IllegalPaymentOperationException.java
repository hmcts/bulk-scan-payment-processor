package uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.exceptions;

public class IllegalPaymentOperationException extends UnrecoverableMessageProcessingException {
    public IllegalPaymentOperationException(String message) {
        super(message);
    }
}
