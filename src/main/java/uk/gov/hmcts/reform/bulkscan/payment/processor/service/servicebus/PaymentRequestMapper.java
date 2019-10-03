package uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bulkscan.payment.processor.client.payhub.request.PaymentRequest;
import uk.gov.hmcts.reform.bulkscan.payment.processor.config.SiteConfiguration;
import uk.gov.hmcts.reform.bulkscan.payment.processor.exception.SiteNotFoundException;
import uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.exceptions.InvalidMessageException;
import uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.model.PaymentMessage;

import java.util.List;
import java.util.stream.Collectors;

@Component
@EnableConfigurationProperties(SiteConfiguration.class)
public class PaymentRequestMapper {

    private final SiteConfiguration siteConfiguration;

    public PaymentRequestMapper(SiteConfiguration siteConfiguration) {
        this.siteConfiguration = siteConfiguration;
    }

    public PaymentRequest mapPaymentMessage(PaymentMessage message) {
        return new PaymentRequest(
            message.ccdCaseNumber,
            getPaymentDocumentControlNumbers(message),
            message.isExceptionRecord,
            getSiteIdForPostCode(message.poBox)
        );
    }

    private List<String> getPaymentDocumentControlNumbers(PaymentMessage message) {
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

    private String getSiteIdForPostCode(String poBox) {
        String siteId = siteConfiguration.getSiteIdByPoBox(poBox);
        if (siteId == null)
            throw new SiteNotFoundException("Site not Found for  po box : " + poBox);

        return siteId;
    }
}
