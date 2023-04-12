package uk.gov.hmcts.reform.bulkscan.payment.processor.models;

public interface SessionAwareMessagingService {

    boolean sendMessage(SessionAwareRequest message);
}
