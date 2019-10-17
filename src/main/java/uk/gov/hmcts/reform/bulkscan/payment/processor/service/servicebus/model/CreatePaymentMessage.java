package uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CreatePaymentMessage extends PaymentMessage {

    public final String ccdReference;
    public final boolean isExceptionRecord;
    public final String poBox;
    public final List<PaymentInfo> payments;

    public CreatePaymentMessage(
        @JsonProperty(value = "envelope_id", required = true) String envelopeId,
        @JsonProperty(value = "jurisdiction", required = true) String jurisdiction,
        @JsonProperty(value = "service", required = true) String service,
        @JsonProperty(value = "ccd_reference", required = true) String ccdReference,
        @JsonProperty(value = "is_exception_record", required = true) boolean isExceptionRecord,
        @JsonProperty(value = "po_box", required = true) String poBox,
        @JsonProperty(value = "payments", required = true) List<PaymentInfo> payments) {
        super(envelopeId, jurisdiction, service);
        this.ccdReference = ccdReference;
        this.isExceptionRecord = isExceptionRecord;
        this.poBox = poBox;
        this.payments = payments;
    }
}
