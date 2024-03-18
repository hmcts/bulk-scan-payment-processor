package uk.gov.hmcts.reform.bulkscan.payment.processor.config;

/**
 * Configuration properties for the queue.
 */
class QueueConfigurationProperties {

    private String accessKey;
    private String accessKeyName;
    private String namespace;
    private String queueName;

    /**
     * Get the access key.
     * @return The access key
     */
    public String getAccessKey() {
        return accessKey;
    }

    /**
     * Get the access key name.
     * @return The access key name
     */
    public String getAccessKeyName() {
        return accessKeyName;
    }

    /**
     * Get the namespace.
     * @return The namespace
     */
    public String getNamespace() {
        return namespace;
    }

    /**
     * Get the queue name.
     * @return The queue name
     */
    public String getQueueName() {
        return queueName;
    }

    /**
     * Set the access key.
     * @param accessKey The access key
     */
    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    /**
     * Set the access key name.
     * @param accessKeyName The access key name
     */
    public void setAccessKeyName(String accessKeyName) {
        this.accessKeyName = accessKeyName;
    }

    /**
     * Set the namespace.
     * @param namespace The namespace
     */
    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    /**
     * Set the queue name.
     * @param queueName The queue name
     */
    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }
}
