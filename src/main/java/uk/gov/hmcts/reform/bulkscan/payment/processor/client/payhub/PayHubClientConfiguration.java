package uk.gov.hmcts.reform.bulkscan.payment.processor.client.payhub;

import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;

public class PayHubClientConfiguration {

    @Bean
    public ErrorDecoder payHubClientErrorDecoder() {
        return new PayHubClientErrorDecoder();
    }
}
