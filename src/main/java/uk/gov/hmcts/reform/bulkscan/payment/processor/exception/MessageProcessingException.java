package uk.gov.hmcts.reform.bulkscan.payment.processor.exception;

import uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.handler.MessageProcessingResultType;

public class MessageProcessingException extends RuntimeException {

    private MessageProcessingResultType messageProcessingResultType;

    public MessageProcessingException(String message, MessageProcessingResultType messageProcessingResult) {
        super(message);
        this.messageProcessingResultType = messageProcessingResult;
    }

    public MessageProcessingResultType getMessageProcessingResultType() {
        return messageProcessingResultType;
    }
}
