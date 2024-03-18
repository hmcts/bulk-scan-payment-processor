package uk.gov.hmcts.reform.bulkscan.payment.processor.client.payhub.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Request to create payment in PayHub.
 */
public class CreatePaymentRequest {

    @JsonProperty("ccd_case_number")
    public final String ccdCaseNumber;

    @JsonProperty("document_control_numbers")
    public final List<String> documentControlNumbers;

    @JsonProperty("is_exception_record")
    public final boolean isExceptionRecord;

    @JsonProperty("site_id")
    public final String siteId;

    /**
     * Constructor.
     * @param ccdCaseNumber The CCD case number
     * @param documentControlNumbers The document control numbers
     * @param isExceptionRecord The exception record flag
     * @param siteId The site ID
     */
    public CreatePaymentRequest(
        String ccdCaseNumber,
        List<String> documentControlNumbers,
        boolean isExceptionRecord,
        String siteId
    ) {
        this.ccdCaseNumber = ccdCaseNumber;
        this.documentControlNumbers = documentControlNumbers;
        this.isExceptionRecord = isExceptionRecord;
        this.siteId = siteId;
    }
}
