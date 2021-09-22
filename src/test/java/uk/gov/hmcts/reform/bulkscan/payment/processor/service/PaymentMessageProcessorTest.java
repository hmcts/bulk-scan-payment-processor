package uk.gov.hmcts.reform.bulkscan.payment.processor.service;

import com.azure.core.util.BinaryData;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;
import com.azure.messaging.servicebus.models.DeadLetterOptions;
import feign.FeignException;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.bulkscan.payment.processor.client.processor.ProcessorClient;
import uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.PaymentMessageParser;
import uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.PaymentMessageProcessor;
import uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.exceptions.InvalidMessageException;
import uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.handler.PaymentMessageHandler;
import uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.model.UpdatePaymentMessage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willReturn;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static uk.gov.hmcts.reform.bulkscan.payment.processor.data.producer.SamplePaymentMessageData.paymentMessage;
import static uk.gov.hmcts.reform.bulkscan.payment.processor.data.producer.SamplePaymentMessageData.paymentMessageJson;
import static uk.gov.hmcts.reform.bulkscan.payment.processor.data.producer.SamplePaymentMessageData.updatePaymentMessageJsonAsString;

@ExtendWith(MockitoExtension.class)
class PaymentMessageProcessorTest {

    private static final String DEAD_LETTER_REASON_PROCESSING_ERROR = "Payment Message processing error";
    private static final String MESSAGE_LABEL_CREATE = "CREATE";
    private static final String MESSAGE_LABEL_UPDATE = "UPDATE";
    private static final String RECOVERABLE_EXCEPTION_MESSAGE = "exception of type treated as recoverable";

    @Mock
    private ServiceBusReceivedMessageContext serviceBusReceivedMessageContext;

    @Mock
    private ServiceBusReceivedMessage message;

    @Mock
    private PaymentMessageHandler paymentMessageHandler;

    @Mock
    private PaymentMessageParser paymentMessageParser;

    @Mock
    private ProcessorClient processorClient;

    private PaymentMessageProcessor paymentMessageProcessor;

    private static final String CCD_CASE_NUMBER = "213132131";

    private static final boolean IS_EXCEPTION_RECORD = true;

    @BeforeEach
    void before() {
        paymentMessageProcessor = new PaymentMessageProcessor(
            paymentMessageHandler,
            paymentMessageParser,
            processorClient,
            10
        );
    }


    @Test
    void should_return_when_there_is_no_message_to_process() {
        // given
        given(serviceBusReceivedMessageContext.getMessage()).willReturn(null);

        // when
        paymentMessageProcessor.processNextMessage(serviceBusReceivedMessageContext);

        // then
        verify(serviceBusReceivedMessageContext).getMessage();
        verifyNoMoreInteractions(serviceBusReceivedMessageContext);
        verify(processorClient, never()).updatePayments(any());
    }

    @Test
    void should_not_throw_exception_when_queue_message_is_invalid() {
        given(serviceBusReceivedMessageContext.getMessage()).willReturn(message);
        var messageBody = BinaryData.fromString("foo");
        given(message.getBody())
            .willReturn(messageBody);
        given(message.getSubject()).willReturn(MESSAGE_LABEL_CREATE);
        given((paymentMessageParser.parse(messageBody)))
            .willThrow(
                new InvalidMessageException("Can't parse")
            );

        paymentMessageProcessor.processNextMessage(serviceBusReceivedMessageContext);
        verify(paymentMessageParser).parse(messageBody);
    }

    @Test
    void should_not_throw_exception_when_payment_handler_fails() throws Exception {
        // given
        var messageBody = setValidMessage(MESSAGE_LABEL_CREATE, paymentJsonString());

        given(serviceBusReceivedMessageContext.getMessage()).willReturn(message);

        given(paymentMessageParser.parse(messageBody)).willReturn(paymentMessage("32131", true));
        // and
        willThrow(new RuntimeException()).given(paymentMessageHandler).handlePaymentMessage(any(), any());

        assertThatCode(() -> paymentMessageProcessor.processNextMessage(serviceBusReceivedMessageContext))
            .doesNotThrowAnyException();
        verify(processorClient, never()).updatePayments(any());
    }

    @Test
    void should_complete_create_message_when_processing_is_successful() throws Exception {
        // given
        setValidMessage(MESSAGE_LABEL_CREATE, paymentJsonString());
        given(serviceBusReceivedMessageContext.getMessage()).willReturn(message);
        willReturn(paymentMessage(CCD_CASE_NUMBER, IS_EXCEPTION_RECORD)).given(paymentMessageParser).parse(any());

        // when
        paymentMessageProcessor.processNextMessage(serviceBusReceivedMessageContext);

        // then
        verify(serviceBusReceivedMessageContext, times(2)).getMessage();
        verify(serviceBusReceivedMessageContext).complete();
        verify(processorClient).updatePayments(any());
    }


    @Test
    void should_complete_update_message_when_processing_is_successful() throws Exception {
        // given

        var messageBody = setValidMessage(
            MESSAGE_LABEL_UPDATE,
            updatePaymentMessageJsonAsString(
                "env-12312",
                "PROBATE",
                "excp-ref-9999",
                "new-case-ref-12312"
            )
        );
        given(serviceBusReceivedMessageContext.getMessage()).willReturn(message);

        willReturn(new UpdatePaymentMessage(
            "env-12312",
            "PROBATE",
            "excp-ref-9999",
            "new-case-ref-12312"
        )).given(paymentMessageParser).parseUpdateMessage(messageBody);


        // when
        paymentMessageProcessor.processNextMessage(serviceBusReceivedMessageContext);

        // then
        verify(serviceBusReceivedMessageContext, times(2)).getMessage();
        verify(serviceBusReceivedMessageContext).complete();
    }

    @Test
    void should_dead_letter_the_message_when_unrecoverable_failure() {
        // given

        given(message.getBody()).willReturn(BinaryData.fromString("invalid body"));
        given(message.getSubject()).willReturn(MESSAGE_LABEL_CREATE);
        willThrow(new InvalidMessageException("JsonParseException")).given(paymentMessageParser).parse(any());

        given(serviceBusReceivedMessageContext.getMessage()).willReturn(message);

        // when
        paymentMessageProcessor.processNextMessage(serviceBusReceivedMessageContext);

        // then
        verify(serviceBusReceivedMessageContext, times(3)).getMessage();

        ArgumentCaptor<DeadLetterOptions> deadLetterOptionsArgumentCaptor
            = ArgumentCaptor.forClass(DeadLetterOptions.class);

        verify(serviceBusReceivedMessageContext).deadLetter(
            deadLetterOptionsArgumentCaptor.capture()
        );

        var deadLetterOptions = deadLetterOptionsArgumentCaptor.getValue();
        assertThat(deadLetterOptions.getDeadLetterReason())
            .isEqualTo("Payment Message processing error");
        assertThat(deadLetterOptions.getDeadLetterErrorDescription())
            .isEqualTo("JsonParseException");

        verifyNoMoreInteractions(serviceBusReceivedMessageContext);
        verify(processorClient, never()).updatePayments(any());
    }

    @Test
    void should_dead_letter_update_message_when_unrecoverable_failure() {
        // given
        var messageBody = setValidMessage(MESSAGE_LABEL_UPDATE, "invalid body");

        willThrow(new InvalidMessageException("JsonParseException"))
            .given(paymentMessageParser).parseUpdateMessage(messageBody);

        given(serviceBusReceivedMessageContext.getMessage()).willReturn(message);

        // when
        paymentMessageProcessor.processNextMessage(serviceBusReceivedMessageContext);

        // then
        verify(serviceBusReceivedMessageContext, times(3)).getMessage();
        ArgumentCaptor<DeadLetterOptions> deadLetterOptionsArgumentCaptor
            = ArgumentCaptor.forClass(DeadLetterOptions.class);

        verify(serviceBusReceivedMessageContext).deadLetter(
            deadLetterOptionsArgumentCaptor.capture()
        );

        var deadLetterOptions = deadLetterOptionsArgumentCaptor.getValue();
        assertThat(deadLetterOptions.getDeadLetterReason())
            .isEqualTo(DEAD_LETTER_REASON_PROCESSING_ERROR);
        assertThat(deadLetterOptions.getDeadLetterErrorDescription())
            .isEqualTo("JsonParseException");

        verifyNoMoreInteractions(serviceBusReceivedMessageContext);
    }

    @Test
    void should_dead_letter_message_when_it_has_no_label() {
        // given
        given(message.getSubject()).willReturn(null); // no label
        given(serviceBusReceivedMessageContext.getMessage()).willReturn(message);

        // when
        paymentMessageProcessor.processNextMessage(serviceBusReceivedMessageContext);

        // then
        verify(serviceBusReceivedMessageContext, times(2)).getMessage();

        ArgumentCaptor<DeadLetterOptions> deadLetterOptionsArgumentCaptor
            = ArgumentCaptor.forClass(DeadLetterOptions.class);

        verify(serviceBusReceivedMessageContext).deadLetter(
            deadLetterOptionsArgumentCaptor.capture()
        );

        var deadLetterOptions = deadLetterOptionsArgumentCaptor.getValue();
        assertThat(deadLetterOptions.getDeadLetterReason())
            .isEqualTo("Missing label");
        assertThat(deadLetterOptions.getDeadLetterErrorDescription())
            .isNull();

        verifyNoMoreInteractions(serviceBusReceivedMessageContext);
    }

    @Test
    void should_not_dead_letter_create_message_when_recoverable_failure() throws Exception {

        var messageBody = setValidMessage(MESSAGE_LABEL_CREATE, paymentJsonString());
        given(serviceBusReceivedMessageContext.getMessage()).willReturn(message);

        given(paymentMessageParser.parse(messageBody))
            .willReturn(paymentMessage(CCD_CASE_NUMBER, IS_EXCEPTION_RECORD));
        Exception processingFailureCause = mock(FeignException.UnprocessableEntity.class);


        // given an error occurs during message processing
        willThrow(processingFailureCause).given(paymentMessageHandler).handlePaymentMessage(any(), any());

        // when
        paymentMessageProcessor.processNextMessage(serviceBusReceivedMessageContext);

        // then the message is not finalised (completed/dead-lettered)
        verify(serviceBusReceivedMessageContext, times(3)).getMessage();
        verify(processorClient, never()).updatePayments(any());
        verifyNoMoreInteractions(serviceBusReceivedMessageContext);
    }

    @Test
    void should_not_dead_letter_update_message_when_recoverable_failure() throws Exception {

        var messageBody = setValidMessage(
            MESSAGE_LABEL_UPDATE,
            updatePaymentMessageJsonAsString(
                "env-12312",
                "PROBATE",
                "excp-ref-9999",
                "new-case-ref-12312"
            )
        );
        given(serviceBusReceivedMessageContext.getMessage()).willReturn(message);

        Exception processingFailureCause = mock(FeignException.UnprocessableEntity.class);

        given(paymentMessageParser.parseUpdateMessage(messageBody))
            .willReturn(new UpdatePaymentMessage(
                            "env-12312",
                            "PROBATE",
                            "excp-ref-9999",
                            "new-case-ref-12312"
                        )
            );
        // given an error occurs during message processing
        willThrow(processingFailureCause).given(paymentMessageHandler).updatePaymentCaseReference(any());

        // when
        paymentMessageProcessor.processNextMessage(serviceBusReceivedMessageContext);

        // then the message is not finalised (completed/dead-lettered)
        verify(serviceBusReceivedMessageContext, times(3)).getMessage();
        verifyNoMoreInteractions(serviceBusReceivedMessageContext);
    }


    @Test
    void should_dead_letter_the_message_when_recoverable_failure_but_delivery_maxed() throws JSONException {
        // given
        var messageBody = setValidMessage(MESSAGE_LABEL_CREATE, paymentJsonString());
        given(serviceBusReceivedMessageContext.getMessage()).willReturn(message);

        given(paymentMessageParser.parse(messageBody))
            .willReturn(paymentMessage(CCD_CASE_NUMBER, IS_EXCEPTION_RECORD));

        paymentMessageProcessor = new PaymentMessageProcessor(
            paymentMessageHandler,
            paymentMessageParser,
            processorClient,
            1
        );
        Exception processingFailureCause = new RuntimeException(RECOVERABLE_EXCEPTION_MESSAGE);

        // and an error occurs during message processing
        willThrow(processingFailureCause).given(paymentMessageHandler).handlePaymentMessage(any(), any());

        // when
        paymentMessageProcessor.processNextMessage(serviceBusReceivedMessageContext);

        // then the message is dead-lettered
        ArgumentCaptor<DeadLetterOptions> deadLetterOptionsArgumentCaptor
            = ArgumentCaptor.forClass(DeadLetterOptions.class);

        verify(serviceBusReceivedMessageContext).deadLetter(
            deadLetterOptionsArgumentCaptor.capture()
        );

        var deadLetterOptions = deadLetterOptionsArgumentCaptor.getValue();
        assertThat(deadLetterOptions.getDeadLetterReason())
            .isEqualTo("Too many deliveries");
        assertThat(deadLetterOptions.getDeadLetterErrorDescription())
            .isEqualTo("Reached limit of message delivery count of 1");

        verify(processorClient, never()).updatePayments(any());
    }

    @Test
    void should_dead_letter_update_message_when_recoverable_failure_but_delivery_maxed() throws Exception {
        // given
        var messageBody = setValidMessage(
            MESSAGE_LABEL_UPDATE,
            updatePaymentMessageJsonAsString(
                "env-12312",
                "PROBATE",
                "excp-ref-9999",
                "new-case-ref-12312"
            )
        );
        given(serviceBusReceivedMessageContext.getMessage()).willReturn(message);

        willReturn(new UpdatePaymentMessage(
            "env-12312",
            "PROBATE",
            "excp-ref-9999",
            "new-case-ref-12312"
        )).given(paymentMessageParser).parseUpdateMessage(messageBody);

        paymentMessageProcessor = new PaymentMessageProcessor(
            paymentMessageHandler,
            paymentMessageParser,
            processorClient,
            1
        );

        Exception processingFailureCause = new RuntimeException(RECOVERABLE_EXCEPTION_MESSAGE);

        // and an error occurs during message processing
        willThrow(processingFailureCause).given(paymentMessageHandler).updatePaymentCaseReference(any());

        // when
        paymentMessageProcessor.processNextMessage(serviceBusReceivedMessageContext);

        // then the message is dead-lettered
        ArgumentCaptor<DeadLetterOptions> deadLetterOptionsArgumentCaptor
            = ArgumentCaptor.forClass(DeadLetterOptions.class);

        verify(serviceBusReceivedMessageContext).deadLetter(
            deadLetterOptionsArgumentCaptor.capture()
        );

        var deadLetterOptions = deadLetterOptionsArgumentCaptor.getValue();
        assertThat(deadLetterOptions.getDeadLetterReason())
            .isEqualTo("Too many deliveries");
        assertThat(deadLetterOptions.getDeadLetterErrorDescription())
            .isEqualTo("Reached limit of message delivery count of 1");

        verify(processorClient, never()).updatePayments(any());
    }

    private BinaryData setValidMessage(String label, String messageStr) {
        var messageBody = BinaryData.fromString(messageStr);
        given(message.getBody()).willReturn(messageBody);
        given(message.getSubject()).willReturn(label);
        return messageBody;
    }

    private String paymentJsonString() throws JSONException {
        return  paymentMessageJson(CCD_CASE_NUMBER, IS_EXCEPTION_RECORD);
    }
}
