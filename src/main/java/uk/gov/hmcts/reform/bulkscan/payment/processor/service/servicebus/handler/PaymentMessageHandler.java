package uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.bulkscan.payment.processor.ccd.CcdClient;
import uk.gov.hmcts.reform.bulkscan.payment.processor.client.payhub.PayHubClient;
import uk.gov.hmcts.reform.bulkscan.payment.processor.client.payhub.PayHubClientException;
import uk.gov.hmcts.reform.bulkscan.payment.processor.client.payhub.request.CaseReferenceRequest;
import uk.gov.hmcts.reform.bulkscan.payment.processor.client.payhub.request.CreatePaymentRequest;
import uk.gov.hmcts.reform.bulkscan.payment.processor.client.payhub.response.CreatePaymentResponse;
import uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.PaymentRequestMapper;
import uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.model.CreatePaymentMessage;
import uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.model.UpdatePaymentMessage;

@Service
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
            ccdClient.completeAwaitingDcnProcessing(
                paymentMessage.ccdReference,
                paymentMessage.service,
                paymentMessage.jurisdiction
            );
        }
    }

    public void updatePaymentCaseReference(UpdatePaymentMessage paymentMessage) {
        CaseReferenceRequest request = new CaseReferenceRequest(paymentMessage.newCaseRef);

        log.info(
            "Sending payment update case reference request, envelope id: {}, case ref: {}, exception ref: {}",
            paymentMessage.envelopeId,
            request.ccdCaseNumber,
            paymentMessage.exceptionRecordRef
        );

        ResponseEntity<?> response = payHubClient.updateCaseReference(
            authTokenGenerator.generate(),
            paymentMessage.exceptionRecordRef,
            request
        );

        log.info(
            "Payment update response from PayHub, envelope id: {}, http status: {}",
            paymentMessage.envelopeId,
            response.getStatusCode()
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
        } catch (PayHubClientException ex) {
            if (ex.getStatus() == HttpStatus.CONFLICT) {
                log.info(
                    "Payment Processed with Http 409, message ID {}. Envelope ID: {}",
                    messageId,
                    paymentMessage.envelopeId
                );
            } else {
                throw ex;
            }
        }
    }
}
