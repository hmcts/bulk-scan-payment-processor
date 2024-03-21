package uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.handler;

/**
 * Represents the result of processing a message.
 */
public class MessageProcessingResult {

    public final MessageProcessingResultType resultType;

    public final Exception exception;

    /**
     * Constructor.
     *
     * @param resultType The result type
     */
    public MessageProcessingResult(MessageProcessingResultType resultType) {
        this(resultType, null);
    }

    /**
     * Constructor.
     *
     * @param resultType The result type
     * @param exception The exception
     */
    public MessageProcessingResult(MessageProcessingResultType resultType, Exception exception) {
        this.resultType = resultType;
        this.exception = exception;
    }
}
