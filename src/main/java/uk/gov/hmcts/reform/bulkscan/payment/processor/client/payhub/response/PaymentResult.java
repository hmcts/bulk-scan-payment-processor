package uk.gov.hmcts.reform.bulkscan.payment.processor.client.payhub.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class PaymentResult {

    public final List<String> paymentDcns;

    public PaymentResult(
        @JsonProperty("payment_dcns") List<String> paymentDcns
    ) {
        this.paymentDcns = paymentDcns;
    }

}
