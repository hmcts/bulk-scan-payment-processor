package uk.gov.hmcts.reform.bulkscan.payment.processor.config;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.PaymentMessageProcessor;

/**
 * Configuration for queue clients.
 */
@Configuration
@ConditionalOnExpression("!${jms.enabled}")
@Profile("!functional & !integration")
public class QueueClientsConfiguration {

    private static final Logger log = LoggerFactory.getLogger(QueueClientsConfiguration.class);

    /**
     * Creates a bean of QueueConfigurationProperties.
     *
     * @return the QueueConfigurationProperties
     */
    @Bean
    @ConfigurationProperties(prefix = "azure.servicebus.payments")
    protected QueueConfigurationProperties paymentQueueConfig() {
        return new QueueConfigurationProperties();
    }

    /**
     * Creates a bean of ServiceBusProcessorClient.
     *
     * @param queueProperties the queue properties
     * @param paymentMessageProcessor the payment message processor
     * @return the ServiceBusProcessorClient
     */
    @Bean
    public ServiceBusProcessorClient notificationsMessageReceiver(
        QueueConfigurationProperties queueProperties,
        PaymentMessageProcessor paymentMessageProcessor
    ) {
        String connectionString  = String.format(
            "Endpoint=sb://%s.servicebus.windows.net;SharedAccessKeyName=%s;SharedAccessKey=%s;",
            queueProperties.getNamespace(),
            queueProperties.getAccessKeyName(),
            queueProperties.getAccessKey()
        );

        return new ServiceBusClientBuilder()
            .connectionString(connectionString)
            .processor()
            .queueName(queueProperties.getQueueName())
            .receiveMode(ServiceBusReceiveMode.PEEK_LOCK)
            .disableAutoComplete()
            .processMessage(paymentMessageProcessor::processNextMessage)
            .processError(c -> log.error("Payment queue handle error {}", c.getErrorSource(), c.getException()))
            .buildProcessorClient();
    }
}
