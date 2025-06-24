package uk.gov.hmcts.reform.bulkscan.payment.processor.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Configuration for async executor.
 */
@Configuration
public class AsyncConfiguration {
    private Logger logger = LoggerFactory.getLogger(AsyncConfiguration.class);

    /**
     * Get the async executor.
     * @param threadPoolSize The thread pool size
     * @return The executor
     */
    @Bean(name = "AsyncExecutor")
    public Executor getExecutor(@Value("${bulk-scan-procesor.async.threadpool-size:5}") int threadPoolSize) {
        logger.info("thread pool size {}", threadPoolSize);
        AtomicInteger count = new AtomicInteger();
        return Executors.newFixedThreadPool(threadPoolSize,
            (Runnable r) -> {
                Thread t = new Thread(r);
                t.setName("AsyncExecutor-" + count.incrementAndGet());
                t.setDaemon(true);
                return t;
            }
        );
    }
}
