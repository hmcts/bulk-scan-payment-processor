package uk.gov.hmcts.reform.bulkscan.payment.processor.client.payhub.request;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request to get case reference from CCD.
 */
public class CaseReferenceRequest {

    @JsonProperty("ccd_case_number")
    public final String ccdCaseNumber;

    /**
     * Constructor.
     * @param ccdCaseNumber The CCD case number
     */
    public CaseReferenceRequest(String ccdCaseNumber) {
        this.ccdCaseNumber = ccdCaseNumber;
    }
}
