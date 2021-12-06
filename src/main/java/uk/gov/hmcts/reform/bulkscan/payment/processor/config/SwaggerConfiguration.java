package uk.gov.hmcts.reform.bulkscan.payment.processor.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfiguration {

    @Bean
    public OpenAPI api() {
        return new OpenAPI()
            .info(new Info().title("Payment Processor API")
                      .description("Payment endpoints")
                      .version("v0.0.1"));
    }

}
