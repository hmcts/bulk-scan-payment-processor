package uk.gov.hmcts.reform.bulkscan.payment.processor.client.payhub.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Response from PayHub when creating a payment.
 */
public class CreatePaymentResponse {

    public final List<String> paymentDcns;

    /**
     * Constructor.
     * @param paymentDcns The payment DCNs
     */
    public CreatePaymentResponse(
        @JsonProperty("payment_dcns") List<String> paymentDcns
    ) {
        this.paymentDcns = paymentDcns;
    }

}
