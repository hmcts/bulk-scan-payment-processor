package uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus;

import com.azure.core.util.BinaryData;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.exceptions.InvalidMessageException;
import uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.model.CreatePaymentMessage;
import uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.model.UpdatePaymentMessage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static uk.gov.hmcts.reform.bulkscan.payment.processor.data.producer.SamplePaymentMessageData.paymentMessage;
import static uk.gov.hmcts.reform.bulkscan.payment.processor.data.producer.SamplePaymentMessageData.paymentMessageJson;


public class PaymentMessageParserTest {

    private ObjectMapper objectMapper = new ObjectMapper();

    private final PaymentMessageParser paymentMessageParser = new PaymentMessageParser(objectMapper);

    @Test
    public void should_return_valid_paymentMessage_when_CreatePaymentMessage_message_is_valid()
        throws JSONException {
        CreatePaymentMessage expected = paymentMessage("232131313121", false);
        CreatePaymentMessage paymentMessage = paymentMessageParser.parse(getValidMessageBody());
        assertThat(paymentMessage).isEqualToComparingFieldByFieldRecursively(expected);
    }

    @Test
    public void should_throw_invalidMessageException_when_createPaymentMessage_message_is_invalid() {
        assertThatThrownBy(
            () -> paymentMessageParser.parse(BinaryData.fromString("parse exception")))
            .isInstanceOf(InvalidMessageException.class);
    }

    private BinaryData getValidMessageBody() throws JSONException {
        return BinaryData.fromString(paymentMessageJson(
            "232131313121",
            false
        ));
    }


    @Test
    public void should_return_valid_updatePaymentMessage_when_queue_message_is_valid() throws JSONException {
        UpdatePaymentMessage expected =
            new UpdatePaymentMessage(
            "envelopeId",
            "Probate",
            "322131",
            "99999"
        );

        UpdatePaymentMessage paymentMessage =
            paymentMessageParser
                .parseUpdateMessage(
                    BinaryData.fromString(
                            getUpdatePaymentMessageJsonString(
                                "envelopeId",
                                "Probate",
                                "322131",
                                "99999"
                            )
                    )
                );

        assertThat(paymentMessage).isEqualToComparingFieldByFieldRecursively(expected);
    }

    @Test
    public void should_throw_invalidMessageException_when_queue_updatePayment_message_is_invalid() {
        assertThatThrownBy(
            () -> paymentMessageParser.parseUpdateMessage(
                BinaryData.fromString("parse exception"))
        ).isInstanceOf(InvalidMessageException.class);
    }

    private static String getUpdatePaymentMessageJsonString(
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

}
