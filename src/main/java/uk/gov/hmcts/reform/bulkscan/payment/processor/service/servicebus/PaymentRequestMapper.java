package uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.reform.bulkscan.payment.processor.client.payhub.request.CreatePaymentRequest;
import uk.gov.hmcts.reform.bulkscan.payment.processor.config.SiteConfiguration;
import uk.gov.hmcts.reform.bulkscan.payment.processor.exception.SiteNotFoundException;
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
            .map(paymentInfo -> paymentInfo.documentControlNumber)
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
