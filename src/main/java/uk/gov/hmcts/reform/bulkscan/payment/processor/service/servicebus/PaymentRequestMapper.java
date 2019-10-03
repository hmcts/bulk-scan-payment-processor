package uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bulkscan.payment.processor.client.payhub.request.PaymentRequest;
import uk.gov.hmcts.reform.bulkscan.payment.processor.config.SiteConfiguration;
import uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.exceptions.SiteNotConfiguredException;
import uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.model.PaymentInfo;
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
            getPaymentDocumentControlNumbers(message.payments),
            message.isExceptionRecord,
            getSiteIdForPostCodeAndJurisdiction(message.poBox, message.jurisdiction)
        );
    }

    private List<String> getPaymentDocumentControlNumbers(List<PaymentInfo> payments) {
        return payments
            .stream()
            .map(paymentInfo -> paymentInfo.documentControlNumber)
            .collect(Collectors.toList());
    }

    private String getSiteIdForPostCodeAndJurisdiction(String poBox, String jurisdiction) {
        return siteConfiguration.getSites()
            .stream().
                filter(
                    config -> config.getPoBox().equalsIgnoreCase(poBox)
                        && config.getSiteName().equalsIgnoreCase(jurisdiction)
                )
            .findFirst()
            .map(SiteConfiguration.Sites::getPoBox)
            .orElseThrow(() ->
                new SiteNotConfiguredException(
                    String.format("Site id is not found for site name: %s PoBox: %s", jurisdiction, poBox)
                )
            );
    }
}
