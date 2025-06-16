package uk.gov.hmcts.reform.bulkscan.payment.processor.errorhandling.exception;

/**
 * Exception to be thrown when payment DCNs are not found.
 */
public class PaymentDcnNotFoundException extends RuntimeException {

    /**
     * Constructor.
     * @param message The message
     */
    public PaymentDcnNotFoundException(String message) {
        super(message);
    }
}
