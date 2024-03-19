package uk.gov.hmcts.reform.bulkscan.payment.processor.task;

import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Task to consume messages from payments queue.
 */
@Service
@ConditionalOnProperty(value = "scheduling.task.consume-payments-queue.enabled", matchIfMissing = true)
@ConditionalOnExpression("!${jms.enabled}")
@Profile("!functional")
public class PaymentMessageProcessTask {

    private static final Logger log = LoggerFactory.getLogger(PaymentMessageProcessTask.class);

    private final ServiceBusProcessorClient serviceBusProcessorClient;

    /**
     * Constructor.
     * @param serviceBusProcessorClient The ServiceBusProcessorClient
     */
    public PaymentMessageProcessTask(
        ServiceBusProcessorClient serviceBusProcessorClient
    ) {
        this.serviceBusProcessorClient = serviceBusProcessorClient;
    }

    /**
     * Start the processor.
     */
    @PostConstruct
    void startProcessor() {
        serviceBusProcessorClient.start();
    }

    /**
     * Check if the processor client is running.
     */
    @Scheduled(fixedDelayString = "${scheduling.task.consume-payments-queue.time-interval-ms}")
    public void checkServiceBusProcessorClient() {
        if (!serviceBusProcessorClient.isRunning()) {
            log.error("Payments queue consume listener is NOT running!!!");
        } else {
            log.info("Payments queue consume listener is working.");
        }
    }

}
