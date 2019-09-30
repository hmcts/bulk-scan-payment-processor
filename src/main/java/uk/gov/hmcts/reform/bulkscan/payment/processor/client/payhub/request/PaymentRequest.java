package uk.gov.hmcts.reform.bulkscan.payment.processor.client.payhub.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class PaymentRequest {

    @JsonProperty("ccd_case_number")
    private final String ccdCaseNumber;

    @JsonProperty("document_control_numbers")
    private final List<String> documentControlNumbers;

    @JsonProperty("is_exception_record")
    private final boolean isExceptionRecord;

    @JsonProperty("site_id")
    private final String siteId;

    public PaymentRequest(String ccdCaseNumber, List<String> documentControlNumbers,
                          boolean isExceptionRecord, String siteId) {
        this.ccdCaseNumber = ccdCaseNumber;
        this.documentControlNumbers = documentControlNumbers;
        this.isExceptionRecord = isExceptionRecord;
        this.siteId = siteId;
    }
}
