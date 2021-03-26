package uk.gov.hmcts.reform.bulkscan.payment.processor.client.processor.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.model.PaymentInfo;

import java.util.List;

public class PaymentRequest {
    @JsonProperty(value = "payments", required = true)
    public final List<PaymentInfo> payments;

    public PaymentRequest(List<PaymentInfo> payments) {
        this.payments = payments;
    }
}
