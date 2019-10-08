package uk.gov.hmcts.reform.bulkscan.payment.processor.client.payhub.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CaseReferenceRequest {

    @JsonProperty("ccd_case_number")
    public final String ccdCaseNumber;

    public CaseReferenceRequest(String ccdCaseNumber) {
        this.ccdCaseNumber = ccdCaseNumber;
    }
}
