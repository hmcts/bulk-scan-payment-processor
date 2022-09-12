package uk.gov.hmcts.reform.bulkscan.payment.processor.task;

import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
@ConditionalOnProperty(value = "scheduling.task.consume-payments-queue.enabled", matchIfMissing = true)
@Profile("!functional")
public class PaymentMessageProcessTask {

    private static final Logger log = LoggerFactory.getLogger(PaymentMessageProcessTask.class);

    private final ServiceBusProcessorClient serviceBusProcessorClient;

    public PaymentMessageProcessTask(
        ServiceBusProcessorClient serviceBusProcessorClient
    ) {
        this.serviceBusProcessorClient = serviceBusProcessorClient;
    }

    @PostConstruct
    void startProcessor() {
        serviceBusProcessorClient.start();
    }

    @Scheduled(fixedDelayString = "${scheduling.task.consume-payments-queue.time-interval-ms}")
    public void checkServiceBusProcessorClient() {
        if (!serviceBusProcessorClient.isRunning()) {
            log.error("Payments queue consume listener is NOT running!!!");
        } else {
            log.info("Payments queue consume listener is working.");
        }
    }

}
