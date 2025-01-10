package uk.gov.hmcts.reform.bulkscan.payment.processor.client.payhub;

import org.springframework.cloud.openfeign.FeignClient;
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

/**
 * PayHub client.
 */
@FeignClient(
    name = "pay-hub-api",
    url = "${pay-hub.api.url}"
)
public interface PayHubClient {

    /**
     * Creates a payment in PayHub.
     *
     * @param serviceAuthorisation service authorisation header
     * @param paymentRequest payment request
     * @return response from PayHub
     */
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

    /**
     * Updates case reference in PayHub.
     *
     * @param serviceAuthorisation service authorisation header
     * @param exceptionReference exception reference
     * @param caseReferenceRequest case reference request
     * @return response from PayHub
     */
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
