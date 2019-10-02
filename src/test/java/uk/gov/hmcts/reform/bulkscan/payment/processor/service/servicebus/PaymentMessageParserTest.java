package uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.microsoft.azure.servicebus.MessageBody;
import com.microsoft.azure.servicebus.MessageBodyType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.bulkscan.payment.processor.data.producer.SamplePaymentMessageData;
import uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.exceptions.InvalidMessageException;
import uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.model.PaymentMessage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
public class PaymentMessageParserTest {

    private ObjectMapper objectMapper = new ObjectMapper();

    private PaymentMessageParser paymentMessageParser;

    @BeforeEach
    public void before() {
        paymentMessageParser = new PaymentMessageParser(objectMapper);
    }

    @Test
    public void parse_valid_paymentMessage() {
        PaymentMessage comingMessage = SamplePaymentMessageData.paymentMessage("232131313121", false);
        PaymentMessage paymentMessage = paymentMessageParser.parse(getValidMessageBody());
        assertThat(paymentMessage).isEqualToComparingFieldByFieldRecursively(comingMessage);
    }

    @Test
    public void throw_exception_when_invalid_paymentMessage() {
        assertThatThrownBy(() -> paymentMessageParser.parse(MessageBody.fromBinaryData(ImmutableList.of("parse exception".getBytes()))))
            .isInstanceOf(InvalidMessageException.class);

    }


    @Test
    public void throw_exception_when_dataEmpty_paymentMessage() {

        MessageBody nullBinaryData = MessageBody.fromSequenceData(ImmutableList.of(ImmutableList.of(new Object())));
        assertThatThrownBy(() -> paymentMessageParser.parse(nullBinaryData))
            .isInstanceOf(InvalidMessageException.class);

    }



    private MessageBody getValidMessageBody() {
        return MessageBody.fromBinaryData(ImmutableList.of(SamplePaymentMessageData.paymentMessageJsonAsByte(
            "232131313121",
            false
        )));


    }

}
