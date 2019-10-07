package uk.gov.hmcts.reform.bulkscan.payment.processor.data.producer;

import com.google.common.collect.ImmutableList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.model.PaymentInfo;
import uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.model.PaymentMessage;

public final class SamplePaymentMessageData {

    public static final String ENVELOPE_ID = "99999ZS";
    public static final String JURSIDICTION = "BULKSCAN";
    public static final String SERVICE = "bulkscan";
    public static final String PO_BOX = "BULKSCAN_PO_BOX";
    public static final String DCN_1 = "xxxyyyzzz";
    public static final String DCN_2 = "ABCDDDDDD";

    private SamplePaymentMessageData() {
    }

    public static String paymentMessageJson(String ccdCaseNumber, boolean isExceptionRecord) throws JSONException {

        return new JSONObject()
            .put("envelope_id", ENVELOPE_ID)
            .put("ccd_case_number", ccdCaseNumber)
            .put("is_exception_record", isExceptionRecord)
            .put("po_box", PO_BOX)
            .put("jurisdiction", JURSIDICTION)
            .put("service", SERVICE)
            .put("payments", new JSONArray()
                .put(new JSONObject().put("document_control_number", DCN_1))
                .put(new JSONObject().put("document_control_number", DCN_2))
            )
            .toString();
    }

    public static PaymentMessage paymentMessage(String ccdCaseNumber, boolean isExceptionRecord) {

        return new PaymentMessage(
            ENVELOPE_ID,
            ccdCaseNumber,
            isExceptionRecord,
            PO_BOX,
            JURSIDICTION,
            SERVICE,
            ImmutableList.of(new PaymentInfo(DCN_1), new PaymentInfo(DCN_2))
        );
    }

    public static byte[] paymentMessageJsonAsByte(
        String ccdCaseNumber,
        boolean isExceptionRecord
    ) throws JSONException {

        return paymentMessageJson(ccdCaseNumber, isExceptionRecord).getBytes();
    }

}
