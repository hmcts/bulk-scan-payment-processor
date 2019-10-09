package uk.gov.hmcts.reform.bulkscan.payment.processor.client.payhub.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class CreatePaymentResponse {

    public final List<String> paymentDcns;

    public CreatePaymentResponse(
        @JsonProperty("payment_dcns") List<String> paymentDcns
    ) {
        this.paymentDcns = paymentDcns;
    }

}
