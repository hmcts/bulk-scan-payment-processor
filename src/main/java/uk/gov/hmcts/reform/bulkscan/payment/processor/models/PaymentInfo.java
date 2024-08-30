package uk.gov.hmcts.reform.bulkscan.payment.processor.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Represents payment info.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class PaymentInfo {

    @JsonProperty(value = "document_control_number", required = true)
    @NotBlank(message = "Document control number is required")
    private final String documentControlNumber;

    @JsonCreator
    public PaymentInfo(@JsonProperty("document_control_number") String documentControlNumber) {
        this.documentControlNumber = documentControlNumber;
    }
}

