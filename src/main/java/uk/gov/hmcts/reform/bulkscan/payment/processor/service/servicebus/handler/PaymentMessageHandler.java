package uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.bulkscan.payment.processor.client.payhub.PayHubClient;
import uk.gov.hmcts.reform.bulkscan.payment.processor.client.payhub.request.CreatePaymentRequest;
import uk.gov.hmcts.reform.bulkscan.payment.processor.client.payhub.response.CreatePaymentResponse;
import uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.PaymentRequestMapper;
import uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.model.PaymentMessage;

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

    public CreatePaymentResponse handlePaymentMessage(PaymentMessage paymentMessage) {
        CreatePaymentRequest request = paymentRequestMapper.mapPaymentMessage(paymentMessage);

        log.info(
            "Sending Payment request with Document Control Numbers: {} Envelope id: {} poBox: {}",
            String.join(", ", request.documentControlNumbers),
            paymentMessage.envelopeId,
            paymentMessage.poBox
        );

        CreatePaymentResponse paymentResult = payHubClient.createPayment(
            authTokenGenerator.generate(),
            request
        ).getBody();

        log.info(
            "Payment response received from PayHub: {} Envelope id: {} Ccd case reference: {}",
            paymentResult == null ? null : String.join(", ", paymentResult.paymentDcns),
            paymentMessage.envelopeId,
            paymentMessage.ccdReference
        );

        return paymentResult;
    }

}
