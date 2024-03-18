package uk.gov.hmcts.reform.bulkscan.payment.processor.client.processor.response;

/**
 * Payment status response.
 */
public class PaymentStatusReponse {
    private String status;

    /**
     * Constructor.
     */
    private PaymentStatusReponse() {
    }

    /**
     * Constructor.
     * @param status The status
     */
    public PaymentStatusReponse(String status) {
        this.status = status;
    }

    /**
     * Get the status.
     * @return The status
     */
    public String getStatus() {
        return status;
    }
}
