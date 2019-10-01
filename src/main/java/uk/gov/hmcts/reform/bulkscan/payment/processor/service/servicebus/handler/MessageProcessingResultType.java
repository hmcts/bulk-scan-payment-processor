package uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.handler;

public enum MessageProcessingResultType {
    SUCCESS,
    UNRECOVERABLE_FAILURE,
    POTENTIALLY_RECOVERABLE_FAILURE
}
