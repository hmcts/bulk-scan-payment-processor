package uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.hmcts.reform.bulkscan.payment.processor.models.PaymentInfo;

import java.util.List;

/**
 * Represents a message that is sent to the payment processor to create a payment.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreatePaymentMessage {

    public final String label;
    public final String envelopeId;
    public final String ccdReference;
    public final boolean isExceptionRecord;
    public final String poBox;
    public final String jurisdiction;
    public final String service;
    public final List<PaymentInfo> payments;

    /**
     * Constructor.
     *
     * @param label The label
     * @param envelopeId The envelope ID
     * @param ccdReference The CCD reference
     * @param isExceptionRecord Whether the record is an exception record
     * @param poBox The PO box
     * @param jurisdiction The jurisdiction
     * @param service The service
     * @param payments The payments
     */
    public CreatePaymentMessage(
        @JsonProperty(value = "label", required = true) String label,
        @JsonProperty(value = "envelope_id", required = true) String envelopeId,
        @JsonProperty(value = "ccd_reference", required = true) String ccdReference,
        @JsonProperty(value = "is_exception_record", required = true) boolean isExceptionRecord,
        @JsonProperty(value = "po_box", required = true) String poBox,
        @JsonProperty(value = "jurisdiction", required = true) String jurisdiction,
        @JsonProperty(value = "service", required = true) String service,
        @JsonProperty(value = "payments", required = true) List<PaymentInfo> payments) {
        this.label = label;
        this.envelopeId = envelopeId;
        this.ccdReference = ccdReference;
        this.isExceptionRecord = isExceptionRecord;
        this.poBox = poBox;
        this.jurisdiction = jurisdiction;
        this.service = service;
        this.payments = payments;
    }
}
