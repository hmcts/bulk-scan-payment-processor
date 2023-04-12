package uk.gov.hmcts.reform.bulkscan.payment.processor.models;

import com.fasterxml.jackson.annotation.JsonIgnore;

public interface SessionAwareRequest {

    @JsonIgnore
    String getSessionId();
}
