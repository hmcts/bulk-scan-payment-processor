package uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.exceptions;

/**
 * UnknownMessageProcessingResultException.
 */
public class UnknownMessageProcessingResultException extends RuntimeException {

    /**
     * Constructor.
     * @param message The message
     */
    public UnknownMessageProcessingResultException(String message) {
        super(message);
    }
}
