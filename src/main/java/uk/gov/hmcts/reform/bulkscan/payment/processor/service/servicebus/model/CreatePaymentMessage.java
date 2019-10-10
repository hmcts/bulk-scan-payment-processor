package uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CreatePaymentMessage {

    public final String envelopeId;
    public final String ccdReference;
    public final boolean isExceptionRecord;
    public final String poBox;
    public final String jurisdiction;
    public final String service;
    public final List<PaymentInfo> payments;

    public CreatePaymentMessage(
        @JsonProperty(value = "envelope_id", required = true) String envelopeId,
        @JsonProperty(value = "ccd_reference", required = true) String ccdReference,
        @JsonProperty(value = "is_exception_record", required = true) boolean isExceptionRecord,
        @JsonProperty(value = "po_box", required = true) String poBox,
        @JsonProperty(value = "jurisdiction", required = true) String jurisdiction,
        @JsonProperty(value = "service", required = true) String service,
        @JsonProperty(value = "payments", required = true) List<PaymentInfo> payments) {
        this.envelopeId = envelopeId;
        this.ccdReference = ccdReference;
        this.isExceptionRecord = isExceptionRecord;
        this.poBox = poBox;
        this.jurisdiction = jurisdiction;
        this.service = service;
        this.payments = payments;
    }

}
