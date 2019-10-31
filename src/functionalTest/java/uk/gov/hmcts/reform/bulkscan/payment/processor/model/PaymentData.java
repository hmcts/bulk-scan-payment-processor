package uk.gov.hmcts.reform.bulkscan.payment.processor.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PaymentData {

    @JsonProperty("document_control_number")
    public final String documentControlNumber;

    public PaymentData(
        String documentControlNumber
    ) {
        this.documentControlNumber = documentControlNumber;
    }
}
