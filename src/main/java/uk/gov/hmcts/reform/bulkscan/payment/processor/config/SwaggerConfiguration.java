package uk.gov.hmcts.reform.bulkscan.payment.processor.config;

import org.springdoc.core.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfiguration {

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
            .group("paymentProcessor-public")
            .pathsToMatch("/controllers/**")
            .build();
    }
}
