package uk.gov.hmcts.reform.bulkscan.payment.processor.client.processor.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.hmcts.reform.bulkscan.payment.processor.models.PaymentInfo;

import java.util.List;

/**
 * Request to create payment in processor.
 */
public class PaymentRequest {
    @JsonProperty(value = "payments", required = true)
    public final List<PaymentInfo> payments;

    /**
     * Constructor.
     * @param payments The payments
     */
    public PaymentRequest(List<PaymentInfo> payments) {
        this.payments = payments;
    }
}
