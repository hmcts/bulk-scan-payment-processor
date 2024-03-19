package uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents payment info
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentInfo {

    @JsonProperty("document_control_number")
    public final String documentControlNumber;

    /**
     * Constructor.
     *
     * @param documentControlNumber The document control number
     */
    public PaymentInfo(@JsonProperty(value = "document_control_number", required = true) String documentControlNumber) {
        this.documentControlNumber = documentControlNumber;
    }

    /**
     * Get the document control number.
     * @return The document control number
     */
    @Override
    public String toString() {
        return documentControlNumber;
    }
}
