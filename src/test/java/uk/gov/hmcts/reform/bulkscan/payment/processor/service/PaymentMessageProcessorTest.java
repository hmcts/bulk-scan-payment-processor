package uk.gov.hmcts.reform.bulkscan.payment.processor.service;

import com.fasterxml.jackson.core.JsonParseException;
import com.google.common.collect.ImmutableList;
import com.microsoft.azure.servicebus.IMessage;
import com.microsoft.azure.servicebus.IMessageReceiver;
import com.microsoft.azure.servicebus.MessageBody;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.PaymentMessageProcessor;
import uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.handler.PaymentMessageHandler;

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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static uk.gov.hmcts.reform.bulkscan.payment.processor.data.producer.SamplePaymentMessageData.paymentMessageJsonAsByte;

@ExtendWith(SpringExtension.class)
public class PaymentMessageProcessorTest {

    private static final String DEAD_LETTER_REASON_PROCESSING_ERROR = "Message processing error";

    @Mock
    private IMessageReceiver messageReceiver;

    @Mock
    private PaymentMessageHandler paymentMessageHandler;

    private PaymentMessageProcessor paymentMessageProcessor;

    @BeforeEach
    public void before() throws Exception {
        paymentMessageProcessor = new PaymentMessageProcessor(
            paymentMessageHandler,
            messageReceiver,
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
        given(messageReceiver.receive()).willReturn(invalidMessage);

        assertThat(paymentMessageProcessor.processNextMessage()).isTrue();
    }

    @Test
    public void should_not_throw_exception_when_payment_handler_fails() throws Exception {
        // given
        willReturn(getValidMessage()).given(messageReceiver).receive();

        // and
        willThrow(new RuntimeException()).given(paymentMessageHandler).handlePaymentMessage(any());

        assertThatCode(() -> paymentMessageProcessor.processNextMessage()).doesNotThrowAnyException();
    }


    @Test
    public void should_complete_the_message_when_processing_is_successful() throws Exception {
        // given
        IMessage validMessage = getValidMessage();
        given(messageReceiver.receive()).willReturn(validMessage);

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
        given(message.getLockToken()).willReturn(UUID.randomUUID());
        given(messageReceiver.receive()).willReturn(message);

        // when
        paymentMessageProcessor.processNextMessage();

        // then
        verify(messageReceiver).receive();

        verify(messageReceiver).deadLetter(
            eq(message.getLockToken()),
            eq(DEAD_LETTER_REASON_PROCESSING_ERROR),
            contains(JsonParseException.class.getSimpleName()),
            any()
        );
        verifyNoMoreInteractions(messageReceiver);
    }


    @Test
    public void should_not_finalize_the_message_when_recoverable_failure() throws Exception {
        willReturn(getValidMessage()).given(messageReceiver).receive();

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
    public void should_finalize_the_message_when_recoverable_failure_but_delivery_maxed() throws Exception {
        // given
        IMessage validMessage = getValidMessage();
        given(messageReceiver.receive()).willReturn(validMessage);

        paymentMessageProcessor = new PaymentMessageProcessor(
            paymentMessageHandler,
            messageReceiver,
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
            eq("Reached limit of message delivery count of 1"),
            any()
        );
    }

    //    @Test
    //    public void should_send_message_with_envelope_id_when_processing_successful() throws Exception {
    //        // given
    //        String envelopeId = UUID.randomUUID().toString();
    //        IMessage message = mock(IMessage.class);
    //        given(message.getMessageBody()).willReturn(
    //            MessageBody.fromBinaryData(ImmutableList.of(envelopeJson(NEW_APPLICATION, "caseRef123", envelopeId)))
    //        );
    //        given(message.getLockToken()).willReturn(UUID.randomUUID());
    //        given(messageReceiver.receive()).willReturn(message);
    //
    //        // when
    //        paymentMessageProcessor.processNextMessage();
    //
    //        // then
    //        verify(processedEnvelopeNotifier).notify(envelopeId);
    //    }

    //    @Test
    //    public void should_not_send_processed_envelope_notification_when_processing_fails() throws Exception {
    //        // given
    //        willReturn(getValidMessage()).given(messageReceiver).receive();
    //
    //        // and
    //        Exception processingFailureCause = new RuntimeException("test exception");
    //        willThrow(processingFailureCause).given(envelopeHandler).handleEnvelope(any());
    //
    //        // when
    //        paymentMessageProcessor.processNextMessage();
    //
    //        // then no notification is sent
    //        verifyNoMoreInteractions(processedEnvelopeNotifier);
    //    }

    @Test
    public void should_throw_exception_when_message_receiver_fails() throws Exception {
        ServiceBusException receiverException = new ServiceBusException(true);
        willThrow(receiverException).given(messageReceiver).receive();

        assertThatThrownBy(() -> paymentMessageProcessor.processNextMessage())
            .isSameAs(receiverException);
    }


    private IMessage getValidMessage() {
        IMessage message = mock(IMessage.class);
        given(message.getMessageBody())
            .willReturn(MessageBody.fromBinaryData(ImmutableList.of(envelopeJson())));
        return message;
    }

    private byte[] envelopeJson() {
        return paymentMessageJsonAsByte("213132131", true);
    }

}
