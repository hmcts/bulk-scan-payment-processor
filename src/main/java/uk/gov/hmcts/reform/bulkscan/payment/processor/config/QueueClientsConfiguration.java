package uk.gov.hmcts.reform.bulkscan.payment.processor.config;

import com.microsoft.azure.servicebus.ClientFactory;
import com.microsoft.azure.servicebus.IMessageReceiver;
import com.microsoft.azure.servicebus.ReceiveMode;
import com.microsoft.azure.servicebus.primitives.ConnectionStringBuilder;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!functional & !integration")
public class QueueClientsConfiguration {

    @Bean
    @ConfigurationProperties(prefix = "azure.servicebus.payments")
    private QueueConfig paymentQueueConfig() {
        return new QueueConfig();
    }

    @Bean
    public IMessageReceiver paymentMessageReceiver(
        QueueConfig queueConfig
    ) throws InterruptedException, ServiceBusException {
        return ClientFactory.createMessageReceiverFromConnectionStringBuilder(
            new ConnectionStringBuilder(
                queueConfig.getNamespace(),
                queueConfig.getQueueName(),
                queueConfig.getAccessKeyName(),
                queueConfig.getAccessKey()
            ),
            ReceiveMode.PEEKLOCK
        );
    }

    private static class QueueConfig {
        private String accessKey;
        private String accessKeyName;
        private String namespace;
        private String queueName;

        public String getAccessKey() {
            return accessKey;
        }

        public String getAccessKeyName() {
            return accessKeyName;
        }

        public String getNamespace() {
            return namespace;
        }

        public String getQueueName() {
            return queueName;
        }

        public void setAccessKey(String accessKey) {
            this.accessKey = accessKey;
        }

        public void setAccessKeyName(String accessKeyName) {
            this.accessKeyName = accessKeyName;
        }

        public void setNamespace(String namespace) {
            this.namespace = namespace;
        }

        public void setQueueName(String queueName) {
            this.queueName = queueName;
        }
    }
}
