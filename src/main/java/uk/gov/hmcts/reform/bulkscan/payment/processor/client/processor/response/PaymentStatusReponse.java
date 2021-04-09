package uk.gov.hmcts.reform.bulkscan.payment.processor.client.processor.response;

public class PaymentStatusReponse {
    private String status;

    private PaymentStatusReponse(){
    }

    public PaymentStatusReponse(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
