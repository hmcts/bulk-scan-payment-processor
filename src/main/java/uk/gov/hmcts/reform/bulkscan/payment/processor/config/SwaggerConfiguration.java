package uk.gov.hmcts.reform.bulkscan.payment.processor.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger configuration.
 */
@Configuration
public class SwaggerConfiguration {

    /**
     * Get the OpenAPI bean.
     * @return The OpenAPI bean
     */
    @Bean
    public OpenAPI api() {
        return new OpenAPI()
            .info(new Info().title("Bulk Scan Payment Processor")
                      .description("API for all operations relating to the BS payment processor application.")
                      .version("1.0.0")
                      .license(new License().name("MIT").url("https://opensource.org/licenses/MIT")))
            .externalDocs(new ExternalDocumentation()
                              .description("README")
                              .url("https://github.com/hmcts/bulk-scan-payment-processor"));
    }
}
