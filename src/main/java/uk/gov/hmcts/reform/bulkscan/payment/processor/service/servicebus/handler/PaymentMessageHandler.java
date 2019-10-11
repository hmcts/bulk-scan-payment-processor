package uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.bulkscan.payment.processor.client.payhub.PayHubClient;
import uk.gov.hmcts.reform.bulkscan.payment.processor.client.payhub.request.CaseReferenceRequest;
import uk.gov.hmcts.reform.bulkscan.payment.processor.client.payhub.request.CreatePaymentRequest;
import uk.gov.hmcts.reform.bulkscan.payment.processor.client.payhub.response.CreatePaymentResponse;
import uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.PaymentRequestMapper;
import uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.model.CreatePaymentMessage;
import uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.model.PaymentMessage;
import uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.model.UpdatePaymentMessage;

@Service
public class PaymentMessageHandler {

    private static final Logger log = LoggerFactory.getLogger(PaymentMessageHandler.class);

    private final AuthTokenGenerator authTokenGenerator;
    private final PaymentRequestMapper paymentRequestMapper;
    private final PayHubClient payHubClient;

    public PaymentMessageHandler(
        AuthTokenGenerator authTokenGenerator,
        PaymentRequestMapper paymentRequestMapper,
        PayHubClient payHubClient
    ) {
        this.authTokenGenerator = authTokenGenerator;
        this.paymentRequestMapper = paymentRequestMapper;
        this.payHubClient = payHubClient;
    }

    public void handlePaymentMessage(PaymentMessage paymentMessage) {
        CreatePaymentMessage createPaymentMessage = (CreatePaymentMessage)paymentMessage;
        CreatePaymentRequest request = paymentRequestMapper.mapPaymentMessage(createPaymentMessage);

        log.info(
            "Sending Payment request with Document Control Numbers: {} Envelope id: {} poBox: {}",
            String.join(", ", request.documentControlNumbers),
            createPaymentMessage.envelopeId,
            createPaymentMessage.poBox
        );

        CreatePaymentResponse paymentResult = payHubClient.createPayment(
            authTokenGenerator.generate(),
            request
        ).getBody();

        log.info(
            "Payment response received from PayHub: {} Envelope id: {} Ccd case reference: {}",
            paymentResult == null ? null : String.join(", ", paymentResult.paymentDcns),
            createPaymentMessage.envelopeId,
            createPaymentMessage.ccdReference
        );

    }


    public void updatePaymentCaseReference(PaymentMessage paymentMessage) {
        UpdatePaymentMessage updatePaymentMessage = (UpdatePaymentMessage)paymentMessage;
        CaseReferenceRequest request = new CaseReferenceRequest(updatePaymentMessage.newCaseRef);

        log.info(
            "Sending payment update case reference request, envelope id: {}, case ref: {}, exception ref: {}",
            updatePaymentMessage.envelopeId,
            request.ccdCaseNumber,
            updatePaymentMessage.exceptionRecordRef
        );

        ResponseEntity<?> response = payHubClient.updateCaseReference(
            authTokenGenerator.generate(),
            updatePaymentMessage.exceptionRecordRef,
            request
        );

        log.info(
            "Payment update response from PayHub, envelope id: {}, http status: {} ",
            updatePaymentMessage.envelopeId,
            response.getStatusCode()
        );

    }

}
