package uk.gov.hmcts.reform.bulkscan.payment.processor.service.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a message to update payment.
 */
public class UpdatePaymentMessage {

    public final String envelopeId;
    public final String jurisdiction;
    public final String exceptionRecordRef;
    public final String newCaseRef;

    /**
     * Constructor.
     *
     * @param envelopeId The envelope ID
     * @param jurisdiction The jurisdiction
     * @param exceptionRecordRef The exception record reference
     * @param newCaseRef The new case reference
     */
    public UpdatePaymentMessage(
        @JsonProperty(value = "envelope_id", required = true) String envelopeId,
        @JsonProperty(value = "jurisdiction", required = true) String jurisdiction,
        @JsonProperty(value = "exception_record_ref", required = true) String exceptionRecordRef,
        @JsonProperty(value = "new_case_ref", required = true) String newCaseRef
    ) {
        this.envelopeId = envelopeId;
        this.jurisdiction = jurisdiction;
        this.exceptionRecordRef = exceptionRecordRef;
        this.newCaseRef = newCaseRef;
    }
}
