package uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.google.common.collect.ImmutableList;
import com.microsoft.azure.servicebus.MessageBody;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.exceptions.InvalidMessageException;
import uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.model.PaymentMessage;

import static com.microsoft.azure.servicebus.MessageBody.fromBinaryData;
import static com.microsoft.azure.servicebus.MessageBody.fromSequenceData;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static uk.gov.hmcts.reform.bulkscan.payment.processor.data.producer.SamplePaymentMessageData.paymentMessage;
import static uk.gov.hmcts.reform.bulkscan.payment.processor.data.producer.SamplePaymentMessageData.paymentMessageJsonAsByte;


public class PaymentMessageParserTest {

    private ObjectMapper objectMapper = new ObjectMapper();

    private PaymentMessageParser paymentMessageParser;

    @BeforeEach
    public void before() {
        paymentMessageParser = new PaymentMessageParser(objectMapper);
    }

    @Test
    public void parse_valid_paymentMessage() {
        PaymentMessage expected = paymentMessage("232131313121", false);
        PaymentMessage paymentMessage = paymentMessageParser.parse(getValidMessageBody());
        assertThat(paymentMessage).isEqualToComparingFieldByFieldRecursively(expected);
    }

    @Test
    public void throw_exception_when_invalid_paymentMessage() {
        assertThatThrownBy(
            () -> paymentMessageParser.parse(fromBinaryData(ImmutableList.of("parse exception".getBytes()))))
            .isInstanceOf(InvalidMessageException.class);
    }

    @Test
    public void throw_exception_when_dataEmpty_paymentMessage() {

        MessageBody nullBinaryData = fromSequenceData(ImmutableList.of(ImmutableList.of(new Object())));
        assertThatThrownBy(
            () -> paymentMessageParser.parse(nullBinaryData))
            .isInstanceOf(InvalidMessageException.class);
    }


    private MessageBody getValidMessageBody() {
        return fromBinaryData(ImmutableList.of(paymentMessageJsonAsByte(
            "232131313121",
            false
        )));


    }

}
