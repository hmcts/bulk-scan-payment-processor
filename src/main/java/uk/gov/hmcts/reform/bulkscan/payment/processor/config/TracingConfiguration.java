package uk.gov.hmcts.reform.bulkscan.payment.processor.config;

import org.springframework.context.annotation.Configuration;

/**
 * Legacy tracing configuration.
 * java-logging 8.x removed the classic Application Insights integration
 * used by this service, so custom TelemetryProcessor wiring is no longer applicable.
 */
@Configuration
public class TracingConfiguration {
}
