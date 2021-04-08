package uk.gov.hmcts.reform.bulkscan.payment.processor.client.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.codec.Decoder;
import feign.jackson.JacksonDecoder;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import uk.gov.hmcts.reform.bulkscan.payment.processor.client.processor.request.PaymentRequest;
import uk.gov.hmcts.reform.bulkscan.payment.processor.client.processor.response.PaymentStatusReponse;

@FeignClient(name = "bulk-scan-processor-api", url = "${bulk-scan-procesor.api.url}",
    configuration = BulkScanProcessorApiProxy.BulkScanConfiguration.class)
@Profile("!functional")
public interface BulkScanProcessorApiProxy {

    @PutMapping(
        path = "/payment/status",
        consumes = MediaType.APPLICATION_JSON_VALUE
    )
    PaymentStatusReponse updateStatus(
        @RequestHeader(name = "ServiceAuthorization", required = false) String serviceAuthHeader,
        @RequestBody PaymentRequest request
    );


    class BulkScanConfiguration {
        @Bean
        Decoder feignDecoder(ObjectMapper objectMapper) {
            return new JacksonDecoder(objectMapper);
        }

        @Bean
        public CustomFeignErrorDecoder customFeignErrorDecoder() {
            return new CustomFeignErrorDecoder();
        }
    }
}
