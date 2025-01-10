package uk.gov.hmcts.reform.bulkscan.payment.processor.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * Represents the details to create a payment in payHub.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class CreatePayment {

    @JsonProperty(value = "envelope_id", required = true)
    @NotBlank(message = "Envelope ID is required")
    private final String envelopeId;

    @JsonProperty(value = "ccd_reference", required = true)
    @NotBlank(message = "CCD Reference is required")
    private final String ccdReference;

    @JsonProperty(value = "is_exception_record", required = true)
    private final boolean isExceptionRecord;

    @JsonProperty(value = "po_box", required = true)
    @NotBlank(message = "PO Box is required")
    private final String poBox;

    @JsonProperty(value = "jurisdiction", required = true)
    @NotBlank(message = "Jurisdiction is required")
    private final String jurisdiction;

    @JsonProperty(value = "service", required = true)
    @NotBlank(message = "Service is required")
    private final String service;

    @JsonProperty(value = "payments", required = true)
    @NotEmpty(message = "Payments list is required")
    private final List<PaymentInfo> payments;

    public CreatePayment(String envelopeId,
                         String ccdReference,
                         boolean isExceptionRecord,
                         String poBox, String jurisdiction,
                         String service,
                         List<PaymentInfo> payments) {
        this.envelopeId = envelopeId;
        this.ccdReference = ccdReference;
        this.isExceptionRecord = isExceptionRecord;
        this.poBox = poBox;
        this.jurisdiction = jurisdiction;
        this.service = service;
        this.payments = payments;
    }
}
