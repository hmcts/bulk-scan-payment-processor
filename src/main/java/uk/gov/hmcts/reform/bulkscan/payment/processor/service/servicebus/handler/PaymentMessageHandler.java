package uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.bulkscan.payment.processor.client.payhub.PayHubClient;
import uk.gov.hmcts.reform.bulkscan.payment.processor.client.payhub.request.PaymentRequest;
import uk.gov.hmcts.reform.bulkscan.payment.processor.client.payhub.response.PaymentResult;
import uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.PaymentRequestMapper;
import uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.model.PaymentMessage;

import java.util.Objects;

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

    public PaymentResult handlePaymentMessage(PaymentMessage paymentMessage) {
        PaymentRequest request = paymentRequestMapper.mapPaymentMessage(paymentMessage);

        log.info(
            "Sending Payment request to PayHub with Document Control Numbers: ",
            String.join(",", request.documentControlNumbers));

        ResponseEntity<PaymentResult> paymentResult = payHubClient.postPayments(
            authTokenGenerator.generate(),
            request
        );

        log.info(
            "Payment response received from PayHub. Document Control Numbers in the response: {}",
            String.join(",", Objects.requireNonNull(paymentResult.getBody()).paymentDcns)
        );

        return paymentResult.getBody();

    }


}
