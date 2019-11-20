package uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bulkscan.payment.processor.client.payhub.request.CreatePaymentRequest;
import uk.gov.hmcts.reform.bulkscan.payment.processor.config.SiteConfiguration;
import uk.gov.hmcts.reform.bulkscan.payment.processor.exception.SiteNotFoundException;
import uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.exceptions.InvalidMessageException;
import uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.model.CreatePaymentMessage;

import java.util.List;
import java.util.stream.Collectors;

@Component
@EnableConfigurationProperties(SiteConfiguration.class)
public class PaymentRequestMapper {

    private final SiteConfiguration siteConfiguration;

    public PaymentRequestMapper(SiteConfiguration siteConfiguration) {
        this.siteConfiguration = siteConfiguration;
    }

    public CreatePaymentRequest mapPaymentMessage(CreatePaymentMessage message) {
        String siteId = siteConfiguration.getSiteIdByPoBox(message.poBox);
        if (siteId == null) {
            throw new SiteNotFoundException(
                String.format(
                    "Site not found for po box: %s. (Jurisdiction: %s. Service: %s)",
                    message.poBox,
                    message.jurisdiction,
                    message.service
                )
            );
        } else {
            return new CreatePaymentRequest(
                message.ccdReference,
                getPaymentDocumentControlNumbers(message),
                message.isExceptionRecord,
                siteId
            );
        }
    }

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

}
