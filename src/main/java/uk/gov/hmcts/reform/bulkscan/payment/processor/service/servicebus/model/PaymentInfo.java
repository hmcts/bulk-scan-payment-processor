package uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentInfo {

    @JsonProperty("document_control_number")
    public final String documentControlNumber;

    public PaymentInfo(@JsonProperty(value = "document_control_number", required = true) String documentControlNumber) {
        this.documentControlNumber = documentControlNumber;
    }

    @Override
    public String toString() {
        return documentControlNumber;
    }
}
