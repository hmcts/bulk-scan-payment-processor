package uk.gov.hmcts.reform.bulkscan.payment.processor.service;

import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.bulkscan.payment.processor.ccd.CcdClient;
import uk.gov.hmcts.reform.bulkscan.payment.processor.client.payhub.PayHubClient;
import uk.gov.hmcts.reform.bulkscan.payment.processor.client.payhub.request.CaseReferenceRequest;
import uk.gov.hmcts.reform.bulkscan.payment.processor.client.payhub.request.CreatePaymentRequest;
import uk.gov.hmcts.reform.bulkscan.payment.processor.client.payhub.response.CreatePaymentResponse;
import uk.gov.hmcts.reform.bulkscan.payment.processor.errorhandling.exception.PayHubCallException;
import uk.gov.hmcts.reform.bulkscan.payment.processor.models.CreatePayment;
import uk.gov.hmcts.reform.bulkscan.payment.processor.models.UpdatePayment;

import static java.lang.String.format;

/**
 * Handles payment messages.
 */
@SuppressWarnings("LoggingSimilarMessage")
@Service
public class PaymentHubHandlerService {

    private static final Logger log = LoggerFactory.getLogger(PaymentHubHandlerService.class);

    private final AuthTokenGenerator authTokenGenerator;
    private final PaymentRequestMapper paymentRequestMapper;
    private final PayHubClient payHubClient;
    private final CcdClient ccdClient;

    /**
     * Constructor.
     * @param authTokenGenerator The auth token generator
     * @param paymentRequestMapper The payment request mapper
     * @param payHubClient The PayHub client
     * @param ccdClient The CCD client
     */
    public PaymentHubHandlerService(
        AuthTokenGenerator authTokenGenerator,
        PaymentRequestMapper paymentRequestMapper,
        PayHubClient payHubClient,
        CcdClient ccdClient
    ) {
        this.authTokenGenerator = authTokenGenerator;
        this.paymentRequestMapper = paymentRequestMapper;
        this.payHubClient = payHubClient;
        this.ccdClient = ccdClient;
    }

    /**
     * Handles creating a new payment with the payment hub.
     * @param createPayment The details of the payment to create.
     */
    public void handleCreatingPayment(CreatePayment createPayment) {
        createPayment(createPayment);

        if (createPayment.isExceptionRecord()) {
            log.info("Sending request to complete awaiting payment DCN processing. Envelope ID: {}",
                     createPayment.getEnvelopeId());
            ccdClient.completeAwaitingDcnProcessing(
                createPayment.getCcdReference(),
                createPayment.getService(),
                createPayment.getJurisdiction()
            );
        }

    }

    /**
     * Creates payment.
     *
     * @param createPayment The model containing all the details to create a payment.
     */
    private void createPayment(CreatePayment createPayment) {
        CreatePaymentRequest request = paymentRequestMapper.mapPayments(createPayment);

        log.info(
            "Sending Payment request with Document Control Numbers: {}, Envelope id: {}, poBox: {}, ccdReference {}",
            String.join(", ", request.documentControlNumbers),
            createPayment.getEnvelopeId(),
            createPayment.getPoBox(),
            createPayment.getCcdReference()
        );

        log.info(System.getenv("pay-hub.api.url"));

        try {
            CreatePaymentResponse paymentResult = payHubClient.createPayment(
                authTokenGenerator.generate(),
                request
            ).getBody();

            log.info(
                "Payment response received from PayHub: {}. Envelope id: {}. Ccd case reference: {}",
                paymentResult == null ? null : String.join(", ", paymentResult.paymentDcns),
                createPayment.getEnvelopeId(),
                createPayment.getCcdReference()
            );
        } catch (FeignException.Conflict exc) {
            log.info(
                "Payment Processed with Http 409, Envelope ID: {}",
                createPayment.getEnvelopeId()
            );
        } catch (FeignException ex) {
            debugPayHubException(ex, "Failed to call 'createPayment'");
            throw new PayHubCallException(
                format(
                    "Failed creating payment. Envelope ID: %s",
                    createPayment.getEnvelopeId()
                ),
                ex
            );
        }
    }

    /**
     * Updates payment case reference.
     *
     * @param updatePayment The model containing the details to update a payment.
     */
    public void updatePaymentCaseReference(UpdatePayment updatePayment) {
        CaseReferenceRequest request = new CaseReferenceRequest(updatePayment.getNewCaseRef());

        log.info(
            "Sending payment update case reference request. Envelope id: {}, Case ref: {}, Exception record ref: {}",
            updatePayment.getEnvelopeId(),
            request.ccdCaseNumber,
            updatePayment.getExceptionRecordRef()
        );

        try {
            ResponseEntity<String> response = payHubClient.updateCaseReference(
                authTokenGenerator.generate(),
                updatePayment.getExceptionRecordRef(),
                request
            );

            log.info(
                "Payments have been reassigned in PayHub. Envelope id: {}, Exception record ref: {}, New case ref: {}"
                + "Update response: {}",
                updatePayment.getEnvelopeId(),
                updatePayment.getExceptionRecordRef(),
                updatePayment.getNewCaseRef(),
                response.getBody()
            );
        } catch (FeignException ex) {
            debugPayHubException(ex, "Failed to call 'updatePaymentCaseReference'");
            throw new PayHubCallException(
                format(
                    "Failed updating payment. Envelope id: %s, Exception record ref: %s, New case ref: %s",
                    updatePayment.getEnvelopeId(),
                    updatePayment.getExceptionRecordRef(),
                    updatePayment.getNewCaseRef()
                ),
                ex
            );
        }
    }

    /**
     * Logs PayHub exception.
     * @param exception The exception
     * @param introMessage The intro message
     */
    private void debugPayHubException(FeignException exception, String introMessage) {
        log.debug(
            "{}. PayHub response: {}",
            introMessage,
            exception.responseBody().map(b -> new String(b.array())).orElseGet(exception::getMessage)
        );
    }
}
