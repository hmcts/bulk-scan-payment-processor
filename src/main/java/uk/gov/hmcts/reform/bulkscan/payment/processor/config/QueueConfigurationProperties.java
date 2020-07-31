package uk.gov.hmcts.reform.bulkscan.payment.processor.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "azure.servicebus.payments")
public class QueueConfigurationProperties {

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
