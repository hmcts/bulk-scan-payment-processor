package uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UpdatePaymentMessage {

    public final String jurisdiction;
    public final String exceptionRecordRef;
    public final String newCaseRef;

    public UpdatePaymentMessage(
        @JsonProperty(value = "jurisdiction", required = true) String jurisdiction,
        @JsonProperty(value = "exception_record_ref", required = true) String exceptionRecordRef,
        @JsonProperty(value = "new_case_ref", required = true) String newCaseRef
    ) {
        this.jurisdiction = jurisdiction;
        this.exceptionRecordRef = exceptionRecordRef;
        this.newCaseRef = newCaseRef;
    }
}
