package uk.gov.hmcts.reform.bulkscan.payment.processor.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Represents the details to update a payment in payHub.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class UpdatePayment {

    @JsonProperty(value = "envelope_id", required = true)
    @NotBlank(message = "Envelope ID is required")
    private final String envelopeId;

    @JsonProperty(value = "jurisdiction", required = true)
    @NotBlank(message = "Jurisdiction is required")
    private final String jurisdiction;

    @JsonProperty(value = "exception_record_ref", required = true)
    @NotBlank(message = "Exception record reference is required")
    private final String exceptionRecordRef;

    @JsonProperty(value = "new_case_ref", required = true)
    @NotBlank(message = "New case reference is required")
    private final String newCaseRef;
}
