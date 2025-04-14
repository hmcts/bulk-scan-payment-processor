package uk.gov.hmcts.reform.bulkscan.payment.processor.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.bulkscan.payment.processor.client.processor.ProcessorClient;
import uk.gov.hmcts.reform.bulkscan.payment.processor.models.CreatePayment;
import uk.gov.hmcts.reform.bulkscan.payment.processor.models.UpdatePayment;

@Service
@Slf4j
@Profile("!functional")
public class PaymentService {

    private final PaymentHubHandlerService paymentHubHandlerService;
    private final ProcessorClient processorClient;


    public PaymentService(PaymentHubHandlerService paymentHubHandlerService, ProcessorClient processorClient) {
        this.paymentHubHandlerService = paymentHubHandlerService;
        this.processorClient = processorClient;
    }

    /**
     * Handle creating a payment, call other services to handle the creating and responses.
     * @param createPayment The details containing the payment to create.
     */
    public void createPayment(CreatePayment createPayment) {
        log.info("Start processing payment creation with ID {}, CCD ID: {}, Exception record: {}",
                 createPayment.getEnvelopeId(),
                 createPayment.getCcdReference(),
                 createPayment.isExceptionRecord()
        );

        paymentHubHandlerService.handleCreatingPayment(createPayment);
        processorClient.updatePayments(createPayment.getPayments());

        log.info("Processed payment message with ID {}, CCD ID: {}, Exception record: {}",
                 createPayment.getEnvelopeId(),
                 createPayment.getCcdReference(),
                 createPayment.isExceptionRecord()
        );
    }

    /**
     * Handle updating a payment, call other services to handle the updating.
     * @param updatePayment The details containing the payment to update.
     */
    public void updatePayment(UpdatePayment updatePayment) {
        log.info("Start processing payment update with ID {}", updatePayment.getEnvelopeId());

        paymentHubHandlerService.updatePaymentCaseReferenceNew(updatePayment);
    }
}
