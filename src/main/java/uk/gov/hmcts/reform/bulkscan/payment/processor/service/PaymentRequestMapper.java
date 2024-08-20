package uk.gov.hmcts.reform.bulkscan.payment.processor.service;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.reform.bulkscan.payment.processor.client.payhub.request.CreatePaymentRequest;
import uk.gov.hmcts.reform.bulkscan.payment.processor.config.SiteConfiguration;
import uk.gov.hmcts.reform.bulkscan.payment.processor.errorhandling.exception.SiteNotFoundException;
import uk.gov.hmcts.reform.bulkscan.payment.processor.models.CreatePayment;
import uk.gov.hmcts.reform.bulkscan.payment.processor.models.PaymentInfo;
import uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.exceptions.InvalidMessageException;
import uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.model.CreatePaymentMessage;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Maps the payment message to the payment request.
 */
@Component
@EnableConfigurationProperties(SiteConfiguration.class)
@Profile("!functional")
public class PaymentRequestMapper {

    private final SiteConfiguration siteConfiguration;

    public PaymentRequestMapper(SiteConfiguration siteConfiguration) {
        this.siteConfiguration = siteConfiguration;
    }

    /**
     * Maps the payment message to the payment request.
     *
     * @param createPayment The model containing the payment details to create.
     * @return The payment request
     */
    public CreatePaymentRequest mapPayments(CreatePayment createPayment) {
        return new CreatePaymentRequest(
            createPayment.getCcdReference(),
            getPaymentDCNs(createPayment),
            createPayment.isExceptionRecord(),
            getSiteIdForPoBox(createPayment.getPoBox())
        );
    }

    /**
     * Get the document control numbers from the payment.
     *
     * @param createPayment The details of the payment to create.
     * @return A list of payment DCNs.
     * @throws InvalidMessageException if no document control numbers are found in the payment message
     */
    private List<String> getPaymentDCNs(CreatePayment createPayment) {
        if (CollectionUtils.isEmpty(createPayment.getPayments())) {
            throw new InvalidMessageException(
                "No Document Control Numbers found in the payment message. MessageId: " + createPayment.getEnvelopeId()
            );
        }

        return createPayment.getPayments()
            .stream()
            .map(PaymentInfo::getDocumentControlNumber)
            .collect(Collectors.toList());
    }

    /**
     * TODO: Remove when service buses and old code removed.
     * Maps the payment message to the payment request.
     *
     * @param message The payment message
     * @return The payment request
     */
    public CreatePaymentRequest mapPaymentMessage(CreatePaymentMessage message) {
        return new CreatePaymentRequest(
            message.ccdReference,
            getPaymentDocumentControlNumbers(message),
            message.isExceptionRecord,
            getSiteIdForPoBox(message.poBox)
        );
    }

    /**
     * TODO: Remove when service buses and old code removed.
     * Get the document control numbers from the payment message.
     *
     * @param message The payment message
     * @return The payment request
     * @throws InvalidMessageException if no document control numbers are found in the payment message
     */
    private List<String> getPaymentDocumentControlNumbers(CreatePaymentMessage message) {
        if (CollectionUtils.isEmpty(message.payments)) {
            throw new InvalidMessageException(
                "No Document Control Numbers found in the payment message. MessageId: " + message.envelopeId
            );
        }

        return message.payments
            .stream()
            .map(PaymentInfo::getDocumentControlNumber)
            .collect(Collectors.toList());
    }

    /**
     * Get the site id for the po box from the site configuration.
     *
     * @param poBox The po box
     * @return The site id
     * @throws SiteNotFoundException if no site id is found for the po box
     */
    private String getSiteIdForPoBox(String poBox) {
        String siteId = siteConfiguration.getSiteIdByPoBox(poBox);
        if (siteId == null) {
            throw new SiteNotFoundException("Site not Found for po box: " + poBox);
        }

        return siteId;
    }
}
