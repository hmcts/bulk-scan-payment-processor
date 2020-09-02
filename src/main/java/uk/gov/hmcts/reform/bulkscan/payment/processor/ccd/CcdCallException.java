package uk.gov.hmcts.reform.bulkscan.payment.processor.ccd;

public class CcdCallException extends RuntimeException {
    public CcdCallException(String errorMessage, Exception cause) {
        super(errorMessage, cause);
    }
}
