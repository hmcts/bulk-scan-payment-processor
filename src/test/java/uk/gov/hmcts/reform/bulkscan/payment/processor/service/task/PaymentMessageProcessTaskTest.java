package uk.gov.hmcts.reform.bulkscan.payment.processor.service.task;

import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.PaymentMessageProcessor;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
public class PaymentMessageProcessTaskTest {

    @Mock
    private PaymentMessageProcessor paymentMessageProcessor;

    private PaymentMessageProcessTask queueConsumeTask;

    @BeforeEach
    public void setUp() {
        queueConsumeTask = new PaymentMessageProcessTask(
            paymentMessageProcessor
        );

    }

    @Test
    public void consumeMessages_processes_messages_until_envelope_processor_returns_false() throws Exception {
        // given
        given(paymentMessageProcessor.processNextMessage()).willReturn(true, true, true, false);

        // when
        queueConsumeTask.consumeMessages();

        // then
        verify(paymentMessageProcessor, times(4)).processNextMessage();
    }


    @Test
    public void consumeMessages_stops_processing_when_envelope_processor_throws_exception() throws Exception {
        // given
        willThrow(new ServiceBusException(true)).given(paymentMessageProcessor).processNextMessage();

        // when
        queueConsumeTask.consumeMessages();

        // then
        verify(paymentMessageProcessor, times(1)).processNextMessage();
    }

    @Test
    public void consumeMessages_stops_processing_when_envelope_processor_throws_interrupted_exception()
        throws Exception {
        // given
        willThrow(new InterruptedException()).given(paymentMessageProcessor).processNextMessage();

        // when
        queueConsumeTask.consumeMessages();

        // then
        verify(paymentMessageProcessor, times(1)).processNextMessage();
    }
}
