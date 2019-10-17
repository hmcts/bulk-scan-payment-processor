package uk.gov.hmcts.reform.bulkscan.payment.processor.service;

import com.fasterxml.jackson.core.JsonParseException;
import com.google.common.collect.ImmutableList;
import com.microsoft.azure.servicebus.IMessage;
import com.microsoft.azure.servicebus.IMessageReceiver;
import com.microsoft.azure.servicebus.MessageBody;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.hmcts.reform.bulkscan.payment.processor.client.payhub.PayHubClientException;
import uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.PaymentMessageParser;
import uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.PaymentMessageProcessor;
import uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.exceptions.InvalidMessageException;
import uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.handler.PaymentMessageHandler;
import uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.model.UpdatePaymentMessage;

import java.nio.charset.Charset;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willReturn;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static uk.gov.hmcts.reform.bulkscan.payment.processor.data.producer.SamplePaymentMessageData.paymentMessage;
import static uk.gov.hmcts.reform.bulkscan.payment.processor.data.producer.SamplePaymentMessageData.paymentMessageJsonAsByte;
import static uk.gov.hmcts.reform.bulkscan.payment.processor.data.producer.SamplePaymentMessageData.updatePaymentMessageJsonAsByte;

@ExtendWith(MockitoExtension.class)
public class PaymentMessageProcessorTest {

    private static final String DEAD_LETTER_REASON_PROCESSING_ERROR = "Payment Message processing error";

    @Mock
    private IMessageReceiver messageReceiver;

    @Mock
    private PaymentMessageHandler paymentMessageHandler;

    @Mock
    private PaymentMessageParser paymentMessageParser;

    private PaymentMessageProcessor paymentMessageProcessor;

    private static final String CCD_CASE_NUMBER = "213132131";

    private static final boolean IS_EXCEPTION_RECORD = true;

    @BeforeEach
    public void before() throws Exception {
        paymentMessageProcessor = new PaymentMessageProcessor(
            paymentMessageHandler,
            messageReceiver,
            paymentMessageParser,
            10
        );
    }

    @Test
    public void should_return_true_when_there_is_a_message_to_process() throws Exception {
        // given
        willReturn(getValidMessage()).given(messageReceiver).receive();

        // when
        boolean processedMessage = paymentMessageProcessor.processNextMessage();

        //verify that json conversion works

        // then
        assertThat(processedMessage).isTrue();
    }

    @Test
    public void should_return_false_when_there_is_no_message_to_process() throws Exception {
        // given
        given(messageReceiver.receive()).willReturn(null);

        // when
        boolean processedMessage = paymentMessageProcessor.processNextMessage();

        // then
        assertThat(processedMessage).isFalse();
    }

    @Test
    public void should_not_throw_exception_when_queue_message_is_invalid() throws Exception {
        IMessage invalidMessage = mock(IMessage.class);
        given(invalidMessage.getMessageBody())
            .willReturn(MessageBody.fromBinaryData(ImmutableList.of("foo".getBytes())));
        given(invalidMessage.getLabel()).willReturn("CREATE");
        given(messageReceiver.receive()).willReturn(invalidMessage);

        assertThat(paymentMessageProcessor.processNextMessage()).isTrue();
    }

    @Test
    public void should_not_throw_exception_when_payment_handler_fails() throws Exception {
        // given
        willReturn(getValidMessage()).given(messageReceiver).receive();
        willReturn(paymentMessage("32131", true)).given(paymentMessageParser).parse(any());

        // and
        willThrow(new RuntimeException()).given(paymentMessageHandler).handlePaymentMessage(any());

        assertThatCode(() -> paymentMessageProcessor.processNextMessage()).doesNotThrowAnyException();
    }

    @Test
    public void should_complete_the_message_when_processing_is_successful() throws Exception {
        // given
        IMessage validMessage = getValidMessage();
        given(messageReceiver.receive()).willReturn(validMessage);
        willReturn(paymentMessage(CCD_CASE_NUMBER, IS_EXCEPTION_RECORD)).given(paymentMessageParser).parse(any());

        // when
        paymentMessageProcessor.processNextMessage();

        // then
        verify(messageReceiver).receive();
        verify(messageReceiver).complete(validMessage.getLockToken());
    }

    @Test
    public void should_complete_update_message_when_processing_is_successful() throws Exception {
        // given
        IMessage validMessage = getValidUpdateMessage(
            "PROBATE",
            "excp-ref-9999",
            "new-case-ref-12312"
        );

        given(messageReceiver.receive()).willReturn(validMessage);

        willReturn(new UpdatePaymentMessage(
            "PROBATE",
            "excp-ref-9999",
            "new-case-ref-12312"
        )).given(paymentMessageParser).parseUpdateMessage(any());


        // when
        paymentMessageProcessor.processNextMessage();

        // then
        verify(messageReceiver).receive();
        verify(messageReceiver).complete(validMessage.getLockToken());
    }

    @Test
    public void should_complete_the_message_when_processing_get_409_PayHubClientException() throws Exception {
        // given
        IMessage validMessage = getValidMessage();
        given(messageReceiver.receive()).willReturn(validMessage);
        willReturn(paymentMessage(CCD_CASE_NUMBER, IS_EXCEPTION_RECORD)).given(paymentMessageParser).parse(any());

        HttpClientErrorException clientException = new HttpClientErrorException(HttpStatus.CONFLICT, "409_CONFLICT");

        willThrow(new PayHubClientException(clientException)).given(paymentMessageHandler).handlePaymentMessage(any());

        // when
        paymentMessageProcessor.processNextMessage();

        // then
        verify(messageReceiver).receive();
        verify(messageReceiver).complete(validMessage.getLockToken());
    }

    @Test
    public void should_dead_letter_the_message_when_unrecoverable_failure() throws Exception {
        // given
        IMessage message = mock(IMessage.class);
        given(message.getMessageBody()).willReturn(
            MessageBody.fromBinaryData(ImmutableList.of("invalid body".getBytes(Charset.defaultCharset())))
        );
        given(message.getLabel()).willReturn("CREATE");
        willThrow(new InvalidMessageException("JsonParseException")).given(paymentMessageParser).parse(any());

        given(message.getLockToken()).willReturn(UUID.randomUUID());
        given(messageReceiver.receive()).willReturn(message);

        // when
        paymentMessageProcessor.processNextMessage();

        // then
        verify(messageReceiver).receive();

        verify(messageReceiver).deadLetter(
            eq(message.getLockToken()),
            eq(DEAD_LETTER_REASON_PROCESSING_ERROR),
            contains(JsonParseException.class.getSimpleName())
        );
        verifyNoMoreInteractions(messageReceiver);
    }

    @Test
    public void should_dead_letter_update_message_when_unrecoverable_failure() throws Exception {
        // given
        IMessage message = mock(IMessage.class);
        given(message.getMessageBody()).willReturn(
            MessageBody.fromBinaryData(ImmutableList.of("invalid body".getBytes(Charset.defaultCharset())))
        );
        given(message.getLabel()).willReturn("UPDATE");

        willThrow(new InvalidMessageException("JsonParseException"))
            .given(paymentMessageParser).parseUpdateMessage(any());

        given(message.getLockToken()).willReturn(UUID.randomUUID());
        given(messageReceiver.receive()).willReturn(message);

        // when
        paymentMessageProcessor.processNextMessage();

        // then
        verify(messageReceiver).receive();

        verify(messageReceiver).deadLetter(
            eq(message.getLockToken()),
            eq(DEAD_LETTER_REASON_PROCESSING_ERROR),
            contains(JsonParseException.class.getSimpleName())
        );
        verifyNoMoreInteractions(messageReceiver);
    }

    @Test
    public void should_dead_letter_message_when_it_has_no_label() throws Exception {
        // given
        var message = mock(IMessage.class);
        given(message.getLabel()).willReturn(null); // no label
        given(message.getLockToken()).willReturn(UUID.randomUUID());

        given(messageReceiver.receive()).willReturn(message);

        // when
        paymentMessageProcessor.processNextMessage();

        // then
        verify(messageReceiver).receive();

        verify(messageReceiver).deadLetter(
            eq(message.getLockToken()),
            eq("Missing label"),
            eq(null)
        );
    }

    @Test
    public void should_not_dead_letter_the_message_when_recoverable_failure() throws Exception {
        willReturn(getValidMessage()).given(messageReceiver).receive();
        willReturn(paymentMessage(CCD_CASE_NUMBER, IS_EXCEPTION_RECORD)).given(paymentMessageParser).parse(any());

        Exception processingFailureCause = new RuntimeException(
            "exception of type treated as recoverable"
        );

        // given an error occurs during message processing
        willThrow(processingFailureCause).given(paymentMessageHandler).handlePaymentMessage(any());

        // when
        paymentMessageProcessor.processNextMessage();

        // then the message is not finalised (completed/dead-lettered)
        verify(messageReceiver).receive();
    }

    @Test
    public void should_not_dead_letter_the_update_message_when_recoverable_failure() throws Exception {
        willReturn(getValidUpdateMessage(
            "PROBATE",
            "excp-ref-9999",
            "new-case-ref-12312"
        )).given(messageReceiver).receive();

        willReturn(new UpdatePaymentMessage(
            "PROBATE",
            "excp-ref-9999",
            "new-case-ref-12312"
        )).given(paymentMessageParser).parseUpdateMessage(any());

        Exception processingFailureCause = new RuntimeException(
            "exception of type treated as recoverable"
        );

        // given an error occurs during message processing
        willThrow(processingFailureCause).given(paymentMessageHandler).updatePaymentCaseReference(any());

        // when
        paymentMessageProcessor.processNextMessage();

        // then the message is not finalised (completed/dead-lettered)
        verify(messageReceiver).receive();
        verify(messageReceiver, never()).deadLetter(any(), any(), any());
    }

    @Test
    public void should_dead_letter_the_message_when_recoverable_failure_but_delivery_maxed() throws Exception {
        // given
        IMessage validMessage = getValidMessage();
        given(messageReceiver.receive()).willReturn(validMessage);
        willReturn(paymentMessage(CCD_CASE_NUMBER, IS_EXCEPTION_RECORD)).given(paymentMessageParser).parse(any());

        paymentMessageProcessor = new PaymentMessageProcessor(
            paymentMessageHandler,
            messageReceiver,
            paymentMessageParser,
            1
        );
        Exception processingFailureCause = new RuntimeException(
            "exception of type treated as recoverable"
        );

        // and an error occurs during message processing
        willThrow(processingFailureCause).given(paymentMessageHandler).handlePaymentMessage(any());

        // when
        paymentMessageProcessor.processNextMessage();

        // then the message is dead-lettered
        verify(messageReceiver).deadLetter(
            eq(validMessage.getLockToken()),
            eq("Too many deliveries"),
            eq("Reached limit of message delivery count of 1")
        );
    }

    @Test
    public void should_dead_letter_update_message_when_recoverable_failure_but_delivery_maxed() throws Exception {
        // given
        IMessage validMessage = getValidUpdateMessage(
            "PROBATE",
            "excp-ref-9999",
            "new-case-ref-12312"
        );

        given(messageReceiver.receive()).willReturn(validMessage);

        willReturn(new UpdatePaymentMessage(
            "PROBATE",
            "excp-ref-9999",
            "new-case-ref-12312"
        )).given(paymentMessageParser).parseUpdateMessage(any());

        paymentMessageProcessor = new PaymentMessageProcessor(
            paymentMessageHandler,
            messageReceiver,
            paymentMessageParser,
            1
        );

        Exception processingFailureCause = new RuntimeException(
            "exception of type treated as recoverable"
        );

        // and an error occurs during message processing
        willThrow(processingFailureCause).given(paymentMessageHandler).updatePaymentCaseReference(any());

        // when
        paymentMessageProcessor.processNextMessage();

        // then the message is dead-lettered
        verify(messageReceiver).deadLetter(
            eq(validMessage.getLockToken()),
            eq("Too many deliveries"),
            eq("Reached limit of message delivery count of 1")
        );
    }

    @Test
    public void should_throw_exception_when_message_receiver_fails() throws Exception {
        ServiceBusException receiverException = new ServiceBusException(true);
        willThrow(receiverException).given(messageReceiver).receive();

        assertThatThrownBy(() -> paymentMessageProcessor.processNextMessage())
            .isSameAs(receiverException);
    }

    private IMessage getValidMessage() throws JSONException {
        IMessage message = mock(IMessage.class);
        given(message.getMessageBody())
            .willReturn(MessageBody.fromBinaryData(ImmutableList.of(paymentJsonToByte())));
        given(message.getLabel()).willReturn("CREATE");
        return message;
    }

    private IMessage getValidUpdateMessage(
        String jurisdiction,
        String exceptionRecordRef,
        String newCaseRef
    ) throws JSONException {
        IMessage message = mock(IMessage.class);

        given(message.getMessageBody())
            .willReturn(
                MessageBody.fromBinaryData(
                    ImmutableList.of(
                        updatePaymentMessageJsonAsByte(
                            jurisdiction,
                            exceptionRecordRef,
                            newCaseRef
                        )
                    )
                )
            );
        given(message.getLabel()).willReturn("UPDATE");
        return message;
    }

    private byte[] paymentJsonToByte() throws JSONException {
        return paymentMessageJsonAsByte(CCD_CASE_NUMBER, IS_EXCEPTION_RECORD);
    }

}
