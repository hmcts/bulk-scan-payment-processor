package uk.gov.hmcts.reform.bulkscan.payment.processor.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.HttpServerErrorException;

@Configuration
public class RetryConfig {

    @Bean("RetryTemplate")
    public RetryTemplate retryTemplate(@Value("${process.api.retries}") int numberOfRetries,
                                       @Value("${process.api.wait-time-in-ms}") long timeToWait) {
        return RetryTemplate.builder()
            .retryOn(HttpServerErrorException.class)
            .maxAttempts(numberOfRetries)
            .exponentialBackoff(timeToWait, ExponentialBackOffPolicy.DEFAULT_MULTIPLIER,
                                ExponentialBackOffPolicy.DEFAULT_MAX_INTERVAL)
            .build();
    }
}
