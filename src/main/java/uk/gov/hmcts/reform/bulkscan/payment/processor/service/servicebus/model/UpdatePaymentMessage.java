package uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UpdatePaymentMessage extends PaymentMessage {

    public final String exceptionRecordRef;
    public final String newCaseRef;

    public UpdatePaymentMessage(
        @JsonProperty(value = "envelope_id", required = true) String envelopeId,
        @JsonProperty(value = "jurisdiction", required = true) String jurisdiction,
        @JsonProperty(value = "service", required = true) String service,
        @JsonProperty(value = "exception_record_ref", required = true) String exceptionRecordRef,
        @JsonProperty(value = "new_case_ref", required = true) String newCaseRef
    ) {
        super(envelopeId, jurisdiction, service);
        this.exceptionRecordRef = exceptionRecordRef;
        this.newCaseRef = newCaseRef;
    }
}
