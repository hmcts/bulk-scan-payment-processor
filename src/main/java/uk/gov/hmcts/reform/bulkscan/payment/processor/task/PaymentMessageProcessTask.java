package uk.gov.hmcts.reform.bulkscan.payment.processor.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.PaymentMessageProcessor;

@Service
@ConditionalOnProperty(value = "scheduling.task.consume-payments-queue.enabled", matchIfMissing = true)
@Profile({"integration", "!nosb"})
public class PaymentMessageProcessTask {

    private static final Logger log = LoggerFactory.getLogger(PaymentMessageProcessTask.class);

    private final PaymentMessageProcessor paymentMessageProcessor;

    public PaymentMessageProcessTask(
        PaymentMessageProcessor paymentMessageProcessor
    ) {
        this.paymentMessageProcessor = paymentMessageProcessor;
    }

    @Scheduled(fixedDelayString = "${scheduling.task.consume-payments-queue.time-interval-ms}")
    public void consumeMessages() {
        log.info("Started the job consuming payment messages");

        try {
            boolean queueMayHaveMessages = true;

            while (queueMayHaveMessages) {
                queueMayHaveMessages = paymentMessageProcessor.processNextMessage();
            }

            log.info("Finished the job consuming payment messages");
        } catch (InterruptedException exception) {
            logTaskError(exception);
            Thread.currentThread().interrupt();
        } catch (Exception exception) {
            logTaskError(exception);
        }
    }

    private void logTaskError(Exception exception) {
        log.error("An error occurred when running the 'consume payment messages' task", exception);
    }
}
