package uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.handler;

public class MessageProcessingResult {

    public final MessageProcessingResultType resultType;

    public final Exception exception;

    public MessageProcessingResult(MessageProcessingResultType resultType) {
        this(resultType, null);
    }

    public MessageProcessingResult(MessageProcessingResultType resultType, Exception exception) {
        this.resultType = resultType;
        this.exception = exception;
    }
}
