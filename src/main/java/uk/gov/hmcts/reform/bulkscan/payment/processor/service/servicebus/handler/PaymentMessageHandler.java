package uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.handler;

import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.bulkscan.payment.processor.ccd.CcdClient;
import uk.gov.hmcts.reform.bulkscan.payment.processor.client.payhub.PayHubCallException;
import uk.gov.hmcts.reform.bulkscan.payment.processor.client.payhub.PayHubClient;
import uk.gov.hmcts.reform.bulkscan.payment.processor.client.payhub.request.CaseReferenceRequest;
import uk.gov.hmcts.reform.bulkscan.payment.processor.client.payhub.request.CreatePaymentRequest;
import uk.gov.hmcts.reform.bulkscan.payment.processor.client.payhub.response.CreatePaymentResponse;
import uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.PaymentRequestMapper;
import uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.model.CreatePaymentMessage;
import uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.model.UpdatePaymentMessage;

import static java.lang.String.format;

/**
 * Handles payment messages.
 */
@Service
@Profile("!functional")
public class PaymentMessageHandler {

    private static final Logger log = LoggerFactory.getLogger(PaymentMessageHandler.class);

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
    public PaymentMessageHandler(
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
     * Handles payment message.
     * @param paymentMessage The payment message
     * @param messageId The message ID
     */
    public void handlePaymentMessage(CreatePaymentMessage paymentMessage, String messageId) {
        createPayment(paymentMessage, messageId);

        if (paymentMessage.isExceptionRecord) {
            ccdClient.completeAwaitingDcnProcessing(
                paymentMessage.ccdReference,
                paymentMessage.service,
                paymentMessage.jurisdiction
            );
        }
    }

    /**
     * Updates payment case reference.
     * @param paymentMessage The payment message
     */
    public void updatePaymentCaseReference(UpdatePaymentMessage paymentMessage) {
        CaseReferenceRequest request = new CaseReferenceRequest(paymentMessage.newCaseRef);

        log.info(
            "Sending payment update case reference request. Envelope id: {}, Case ref: {}, Exception record ref: {}",
            paymentMessage.envelopeId,
            request.ccdCaseNumber,
            paymentMessage.exceptionRecordRef
        );

        try {
            payHubClient.updateCaseReference(
                authTokenGenerator.generate(),
                paymentMessage.exceptionRecordRef,
                request
            );

            log.info(
                "Payments have been reassigned in PayHub. Envelope id: {}, Exception record ref: {}, New case ref: {}",
                paymentMessage.envelopeId,
                paymentMessage.exceptionRecordRef,
                paymentMessage.newCaseRef
            );
        } catch (FeignException ex) {
            debugPayHubException(ex, "Failed to call 'updatePaymentCaseReference'");
            throw new PayHubCallException(
                format(
                    "Failed updating payment. Envelope id: %s, Exception record ref: %s, New case ref: %s",
                    paymentMessage.envelopeId,
                    paymentMessage.exceptionRecordRef,
                    paymentMessage.newCaseRef
                ),
                ex
            );
        }
    }

    /**
     * Creates payment.
     * @param paymentMessage The payment message
     * @param messageId The message ID
     */
    private void createPayment(CreatePaymentMessage paymentMessage, String messageId) {
        CreatePaymentRequest request = paymentRequestMapper.mapPaymentMessage(paymentMessage);

        log.info(
            "Sending Payment request with Document Control Numbers: {}, Envelope id: {}, poBox: {}, ccdReference {}",
            String.join(", ", request.documentControlNumbers),
            paymentMessage.envelopeId,
            paymentMessage.poBox,
            paymentMessage.ccdReference
        );

        try {
            CreatePaymentResponse paymentResult = payHubClient.createPayment(
                authTokenGenerator.generate(),
                request
            ).getBody();

            log.info(
                "Payment response received from PayHub: {}. Envelope id: {}. Ccd case reference: {}",
                paymentResult == null ? null : String.join(", ", paymentResult.paymentDcns),
                paymentMessage.envelopeId,
                paymentMessage.ccdReference
            );
        } catch (FeignException.Conflict exc) {
            log.info(
                "Payment Processed with Http 409, message ID {}. Envelope ID: {}",
                messageId,
                paymentMessage.envelopeId
            );
        } catch (FeignException ex) {
            debugPayHubException(ex, "Failed to call 'createPayment'");
            throw new PayHubCallException(
                format(
                    "Failed creating payment, message ID %s. Envelope ID: %s",
                    messageId,
                    paymentMessage.envelopeId
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
