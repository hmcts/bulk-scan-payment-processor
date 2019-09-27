package uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentInfo {

    public final String documentControlNumbers;

    public PaymentInfo(@JsonProperty(value = "document_control_numbers", required = true)
                           String documentControlNumbers) {
        this.documentControlNumbers = documentControlNumbers;
    }


}
