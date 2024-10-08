package uk.gov.hmcts.reform.bulkscan.payment.processor.data.producer;

import com.google.common.collect.ImmutableList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import uk.gov.hmcts.reform.bulkscan.payment.processor.models.PaymentInfo;
import uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.model.CreatePaymentMessage;

public final class SamplePaymentMessageData {

    public static final String ENVELOPE_ID = "99999ZS";
    public static final String JURISDICTION = "BULKSCAN";
    public static final String SERVICE = "bulkscan";
    public static final String PO_BOX = "BULKSCAN_PO_BOX";
    public static final String DCN_1 = "xxxyyyzzz";
    public static final String DCN_2 = "ABCDDDDDD";

    private SamplePaymentMessageData() {
    }

    public static String paymentMessageJson(String ccdCaseNumber, boolean isExceptionRecord, String label)
        throws JSONException {

        return new JSONObject()
            .put("label", label)
            .put("envelope_id", ENVELOPE_ID)
            .put("ccd_reference", ccdCaseNumber)
            .put("is_exception_record", isExceptionRecord)
            .put("po_box", PO_BOX)
            .put("jurisdiction", JURISDICTION)
            .put("service", SERVICE)
            .put("payments", new JSONArray()
                .put(new JSONObject().put("document_control_number", DCN_1))
                .put(new JSONObject().put("document_control_number", DCN_2))
            )
            .toString();
    }

    public static String updatePaymentMessageJsonAsString(
        String envelopeId,
        String jurisdiction,
        String exceptionRecordRef,
        String newCaseRef
    ) throws JSONException {

        return new JSONObject()
            .put("envelope_id", envelopeId)
            .put("jurisdiction", jurisdiction)
            .put("exception_record_ref", exceptionRecordRef)
            .put("new_case_ref", newCaseRef)
            .toString();
    }

    public static CreatePaymentMessage paymentMessage(String ccdCaseNumber, boolean isExceptionRecord, String label) {

        return new CreatePaymentMessage(
            label,
            ENVELOPE_ID,
            ccdCaseNumber,
            isExceptionRecord,
            PO_BOX,
            JURISDICTION,
            SERVICE,
            ImmutableList.of(new PaymentInfo(DCN_1), new PaymentInfo(DCN_2))
        );
    }

    public static byte[] paymentMessageJsonAsByte(
        String ccdCaseNumber,
        boolean isExceptionRecord,
        String label
    ) throws JSONException {

        return paymentMessageJson(ccdCaseNumber, isExceptionRecord, label).getBytes();
    }

}
