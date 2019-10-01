package uk.gov.hmcts.reform.bulkscan.payment.processor.data.producer;

import org.json.JSONArray;
import org.json.JSONObject;

public class SamplePaymentMessageData {


    public static final String JURSIDICTION = "BULKSCAN";
    public static final String PO_BOX = "BULKSCAN_PO_BOX";

    private SamplePaymentMessageData() {
    }

    public static String paymentMessageJson(String ccdCaseNumber, boolean isExceptionRecord) {
        try {
            return new JSONObject()
                .put("envelope_id", ccdCaseNumber)
                .put("ccd_case_number", ccdCaseNumber)
                .put("is_exception_record", isExceptionRecord)
                .put("po_box", PO_BOX)
                .put("jurisdiction", JURSIDICTION)
                .put("payments", new JSONArray()
                    .put(new JSONObject()
                             .put("document_control_number", "xxxyyyzzz")
                             .put("document_control_number", "zzzyyyxxx"))
                )
                .toString();
        } catch (Exception e) {
            throw new RuntimeException("Could not make paymentMessageJson", e);
        }

    }

    public static byte[] paymentMessageJsonAsByte(String ccdCaseNumber, boolean isExceptionRecord) {
        return paymentMessageJson(ccdCaseNumber, isExceptionRecord).getBytes();
    }

}
