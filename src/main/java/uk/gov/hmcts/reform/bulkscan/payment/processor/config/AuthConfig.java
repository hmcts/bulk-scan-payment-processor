package uk.gov.hmcts.reform.bulkscan.payment.processor.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.authorisation.ServiceAuthorisationApi;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGeneratorFactory;

@Configuration
public class AuthConfig {

    @Bean
    public AuthTokenGenerator authTokenGenerator(
        @Value("${idam.s2s-auth.secret}") String secret,
        @Value("${idam.s2s-auth.name}") String name,
        ServiceAuthorisationApi serviceAuthorisationApi
    ) {
        return AuthTokenGeneratorFactory.createDefaultGenerator(secret, name, serviceAuthorisationApi);
    }
}
