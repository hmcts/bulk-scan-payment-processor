package uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.google.common.collect.ImmutableList;
import com.microsoft.azure.servicebus.MessageBody;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.exceptions.InvalidMessageException;
import uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.model.CreatePaymentMessage;
import uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.model.UpdatePaymentMessage;

import static com.microsoft.azure.servicebus.MessageBody.fromBinaryData;
import static com.microsoft.azure.servicebus.MessageBody.fromSequenceData;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static uk.gov.hmcts.reform.bulkscan.payment.processor.data.producer.SamplePaymentMessageData.paymentMessage;
import static uk.gov.hmcts.reform.bulkscan.payment.processor.data.producer.SamplePaymentMessageData.paymentMessageJsonAsByte;


public class PaymentMessageParserTest {

    private ObjectMapper objectMapper = new ObjectMapper();

    private final PaymentMessageParser paymentMessageParser = new PaymentMessageParser(objectMapper);

    @Test
    public void should_return_valid_paymentMessage_when_queue_message_is_valid() throws JSONException {
        CreatePaymentMessage expected = paymentMessage("232131313121", false);
        CreatePaymentMessage paymentMessage = paymentMessageParser.parse(
            getValidMessageBody(),
            CreatePaymentMessage.class
        );
        assertThat(paymentMessage).isEqualToComparingFieldByFieldRecursively(expected);
    }

    @Test
    public void should_throw_invalidMessageException_when_queue_message_is_invalid() {
        assertThatThrownBy(
            () -> paymentMessageParser.parse(
                fromBinaryData(ImmutableList.of("parse exception".getBytes())),
                CreatePaymentMessage.class
            ))
            .isInstanceOf(InvalidMessageException.class);
    }

    @Test
    public void should_throw_InvalidMessageException_when_queue_message_is_null() {

        MessageBody nullBinaryData = fromSequenceData(ImmutableList.of(ImmutableList.of(new Object())));
        assertThatThrownBy(
            () -> paymentMessageParser.parse(nullBinaryData, CreatePaymentMessage.class))
            .isInstanceOf(InvalidMessageException.class)
            .hasMessage("Message Binary data is null");
        ;
    }

    private MessageBody getValidMessageBody() throws JSONException {
        return fromBinaryData(ImmutableList.of(paymentMessageJsonAsByte(
            "232131313121",
            false
        )));
    }


    @Test
    public void should_return_valid_updatePaymentMessage_when_queue_message_is_valid() throws JSONException {
        UpdatePaymentMessage expected = new UpdatePaymentMessage(
            "envelopeId",
            "Probate",
            "probate",
            "322131",
            "99999"
        );

        UpdatePaymentMessage paymentMessage =
            paymentMessageParser
                .parse(
                    fromBinaryData(
                        ImmutableList.of(
                            getUpdatePaymentMessageJsonString(
                                "envelopeId",
                                "Probate",
                                "probate",
                                "322131",
                                "99999"
                            ).getBytes()
                        )
                    ),
                    UpdatePaymentMessage.class
                );

        assertThat(paymentMessage).isEqualToComparingFieldByFieldRecursively(expected);
    }

    @Test
    public void should_throw_invalidMessageException_when_queue_updatePayment_message_is_invalid() {
        assertThatThrownBy(
            () -> paymentMessageParser.parse(
                fromBinaryData(ImmutableList.of("parse exception".getBytes())),
                CreatePaymentMessage.class
            )
        ).isInstanceOf(InvalidMessageException.class);
    }

    @Test
    public void should_throw_InvalidMessageException_when_queue_updatePayment_is_null() {

        MessageBody nullBinaryData = fromSequenceData(ImmutableList.of(ImmutableList.of(new Object())));
        assertThatThrownBy(
            () -> paymentMessageParser.parse(nullBinaryData, UpdatePaymentMessage.class))
            .isInstanceOf(InvalidMessageException.class)
            .hasMessage("Message Binary data is null");
        ;
    }

    private static String getUpdatePaymentMessageJsonString(
        String envelopeId,
        String jurisdiction,
        String service,
        String exceptionRecordRef,
        String newCaseRef
    ) throws JSONException {

        return new JSONObject()
            .put("envelope_id", envelopeId)
            .put("jurisdiction", jurisdiction)
            .put("service", service)
            .put("exception_record_ref", exceptionRecordRef)
            .put("new_case_ref", newCaseRef)
            .toString();
    }

}
