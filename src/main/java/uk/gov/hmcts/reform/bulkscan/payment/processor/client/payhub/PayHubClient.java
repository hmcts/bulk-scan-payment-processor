package uk.gov.hmcts.reform.bulkscan.payment.processor.client.payhub;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.hmcts.reform.bulkscan.payment.processor.client.payhub.request.CaseReferenceRequest;
import uk.gov.hmcts.reform.bulkscan.payment.processor.client.payhub.request.CreatePaymentRequest;
import uk.gov.hmcts.reform.bulkscan.payment.processor.client.payhub.response.CreatePaymentResponse;

@FeignClient(
    name = "pay-hub-api",
    url = "${pay-hub.api.url}",
    configuration = PayHubClientConfiguration.class
)
@Profile("!nosb")
public interface PayHubClient {

    @RequestMapping(
        method = RequestMethod.POST,
        value = "/bulk-scan-payments",
        consumes = MimeTypeUtils.APPLICATION_JSON_VALUE,
        produces = MimeTypeUtils.APPLICATION_JSON_VALUE
    )
    ResponseEntity<CreatePaymentResponse> createPayment(
        @RequestHeader("ServiceAuthorization") String serviceAuthorisation,
        @RequestBody CreatePaymentRequest paymentRequest
    );

    @RequestMapping(
        method = RequestMethod.PUT,
        value = "/bulk-scan-payments",
        consumes = MimeTypeUtils.APPLICATION_JSON_VALUE,
        produces = MimeTypeUtils.APPLICATION_JSON_VALUE
    )
    ResponseEntity<Void> updateCaseReference(
        @RequestHeader("ServiceAuthorization") String serviceAuthorisation,
        @RequestParam("exception_reference") String exceptionReference,
        @RequestBody CaseReferenceRequest caseReferenceRequest
    );

}
