package uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.model.jms;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.model.PaymentInfo;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentMessage {

    public String envelopeId;
    public String ccdReference;
    public boolean isExceptionRecord;
    public String poBox;
    public String jurisdiction;
    public String service;
    public List<PaymentInfo> payments;

    public String getEnvelopeId() {
        return envelopeId;
    }

    public void setEnvelopeId(String envelopeId) {
        this.envelopeId = envelopeId;
    }

    public String getCcdReference() {
        return ccdReference;
    }

    public void setCcdReference(String ccdReference) {
        this.ccdReference = ccdReference;
    }

    public boolean isExceptionRecord() {
        return isExceptionRecord;
    }

    public void setExceptionRecord(boolean exceptionRecord) {
        isExceptionRecord = exceptionRecord;
    }

    public String getPoBox() {
        return poBox;
    }

    public void setPoBox(String poBox) {
        this.poBox = poBox;
    }

    public String getJurisdiction() {
        return jurisdiction;
    }

    public void setJurisdiction(String jurisdiction) {
        this.jurisdiction = jurisdiction;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public List<PaymentInfo> getPayments() {
        return payments;
    }

    public void setPayments(List<PaymentInfo> payments) {
        this.payments = payments;
    }
}
