package uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.handler;

import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.bulkscan.payment.processor.ccd.CcdClient;
import uk.gov.hmcts.reform.bulkscan.payment.processor.client.payhub.PayHubClient;
import uk.gov.hmcts.reform.bulkscan.payment.processor.client.payhub.request.CaseReferenceRequest;
import uk.gov.hmcts.reform.bulkscan.payment.processor.client.payhub.request.CreatePaymentRequest;
import uk.gov.hmcts.reform.bulkscan.payment.processor.client.payhub.response.CreatePaymentResponse;
import uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.PaymentRequestMapper;
import uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.model.CreatePaymentMessage;
import uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.model.UpdatePaymentMessage;

@Service
@Profile("!functional")
public class PaymentMessageHandler {

    private static final Logger log = LoggerFactory.getLogger(PaymentMessageHandler.class);

    private final AuthTokenGenerator authTokenGenerator;
    private final PaymentRequestMapper paymentRequestMapper;
    private final PayHubClient payHubClient;
    private final CcdClient ccdClient;

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

    public void handlePaymentMessage(CreatePaymentMessage paymentMessage, String messageId) {
        createPayment(paymentMessage, messageId);

        if (paymentMessage.isExceptionRecord) {
            try {
                ccdClient.completeAwaitingDcnProcessing(
                    paymentMessage.ccdReference,
                    paymentMessage.service,
                    paymentMessage.jurisdiction
                );
            } catch (FeignException exception) {
                log.debug(
                    "Failed to call 'completeAwaitingDcnProcessing' method. CCD response: {}",
                    exception.responseBody().map(b -> new String(b.array())).orElseGet(exception::getMessage)
                );

                throw exception;
            }
        }
    }

    public void updatePaymentCaseReference(UpdatePaymentMessage paymentMessage) {
        CaseReferenceRequest request = new CaseReferenceRequest(paymentMessage.newCaseRef);

        log.info(
            "Sending payment update case reference request. Envelope id: {}, Case ref: {}, Exception record ref: {}",
            paymentMessage.envelopeId,
            request.ccdCaseNumber,
            paymentMessage.exceptionRecordRef
        );

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
    }

    private void createPayment(CreatePaymentMessage paymentMessage, String messageId) {
        CreatePaymentRequest request = paymentRequestMapper.mapPaymentMessage(paymentMessage);

        log.info(
            "Sending Payment request with Document Control Numbers: {}, Envelope id: {}, poBox: {}",
            String.join(", ", request.documentControlNumbers),
            paymentMessage.envelopeId,
            paymentMessage.poBox
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
        }
    }
}
