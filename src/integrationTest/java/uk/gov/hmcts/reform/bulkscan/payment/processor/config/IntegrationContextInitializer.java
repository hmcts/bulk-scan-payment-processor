package uk.gov.hmcts.reform.bulkscan.payment.processor.config;

import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.github.tomakehurst.wiremock.common.Slf4jNotifier;
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import static org.mockito.Mockito.mock;
import static org.springframework.test.util.TestSocketUtils.findAvailableTcpPort;

@Configuration
@Profile("integration") // no servicebus queue handler registration
public class IntegrationContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    public static final String PROFILE_WIREMOCK = "wiremock";

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        System.setProperty("wiremock.port", Integer.toString(findAvailableTcpPort()));
    }

    @Bean
    public Options options(@Value("${wiremock.port}") int port) {
        return WireMockConfiguration.options().port(port).notifier(new Slf4jNotifier(false));
    }

    @Bean
    public ServiceBusProcessorClient paymentServiceBusProcessorClient() {
        return mock(ServiceBusProcessorClient.class);
    }

}
