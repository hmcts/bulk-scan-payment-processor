package uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bulkscan.payment.processor.client.payhub.request.PaymentRequest;
import uk.gov.hmcts.reform.bulkscan.payment.processor.config.SiteConfiguration;
import uk.gov.hmcts.reform.bulkscan.payment.processor.exception.SiteNotFoundException;
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
            getSiteIdForPostCode(message.poBox)
        );
    }

    private List<String> getPaymentDocumentControlNumbers(List<PaymentInfo> payments) {
        return payments
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
