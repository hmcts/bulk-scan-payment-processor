package uk.gov.hmcts.reform.bulkscan.payment.processor.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.HttpServerErrorException;

import static org.springframework.retry.backoff.ExponentialBackOffPolicy.DEFAULT_MAX_INTERVAL;
import static org.springframework.retry.backoff.ExponentialBackOffPolicy.DEFAULT_MULTIPLIER;

/**
 * Configuration for retry template.
 */
@Configuration
public class RetryConfig {

    /**
     * Get the retry template.
     * @param numberOfRetries The number of retries
     * @param timeToWait The time to wait
     * @return The retry template
     */
    @Bean("RetryTemplate")
    public RetryTemplate retryTemplate(@Value("${bulk-scan-procesor.api.retries}") int numberOfRetries,
                                       @Value("${bulk-scan-procesor.api.wait-time-in-ms}") long timeToWait) {
        return RetryTemplate.builder()
                .retryOn(HttpServerErrorException.class)
                .maxAttempts(numberOfRetries)
                .exponentialBackoff(timeToWait, DEFAULT_MULTIPLIER, DEFAULT_MAX_INTERVAL)
                .build();
    }
}
