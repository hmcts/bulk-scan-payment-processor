package uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.model;

public abstract class PaymentMessage {
    public final String envelopeId;
    public final String jurisdiction;
    public final String service;

    protected PaymentMessage(String envelopeId, String jurisdiction, String service) {
        this.envelopeId = envelopeId;
        this.jurisdiction = jurisdiction;
        this.service = service;
    }
}
