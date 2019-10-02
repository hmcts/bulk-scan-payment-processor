package uk.gov.hmcts.reform.bulkscan.payment.processor.client.payhub;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import uk.gov.hmcts.reform.bulkscan.payment.processor.client.payhub.request.PaymentRequest;
import uk.gov.hmcts.reform.bulkscan.payment.processor.client.payhub.response.PaymentResult;

@FeignClient(
    name = "pay-hub-api",
    url = "${pay-hub.api.url}",
    configuration = PayHubClientConfiguration.class
)
public interface PayHubClient {

    @RequestMapping(
        method = RequestMethod.POST,
        value = "/bulk-scan-payments",
        consumes = MimeTypeUtils.APPLICATION_JSON_VALUE,
        produces = MimeTypeUtils.APPLICATION_JSON_VALUE
    )
    ResponseEntity<PaymentResult> postPayments(
        @RequestHeader("ServiceAuthorization") String serviceAuthorisation,
        @RequestBody PaymentRequest paymentRequest
    );
}
