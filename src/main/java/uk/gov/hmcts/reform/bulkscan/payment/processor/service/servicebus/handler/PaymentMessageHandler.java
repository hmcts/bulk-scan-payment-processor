package uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.handler;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.bulkscan.payment.processor.client.payhub.PayHubClientException;
import uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.model.PaymentMessage;


@Service
public class PaymentMessageHandler {



    public PaymentMessageHandler() {
    }

    public void handlePaymentMessage(PaymentMessage paymentMessage) throws PayHubClientException {

       //Handle message
        //call the pay hub

    }
}
