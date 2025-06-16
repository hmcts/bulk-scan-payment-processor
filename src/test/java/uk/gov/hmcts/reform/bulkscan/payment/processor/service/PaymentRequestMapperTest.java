package uk.gov.hmcts.reform.bulkscan.payment.processor.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.bulkscan.payment.processor.client.payhub.request.CreatePaymentRequest;
import uk.gov.hmcts.reform.bulkscan.payment.processor.config.SiteConfiguration;
import uk.gov.hmcts.reform.bulkscan.payment.processor.errorhandling.exception.PaymentDcnNotFoundException;
import uk.gov.hmcts.reform.bulkscan.payment.processor.errorhandling.exception.SiteNotFoundException;
import uk.gov.hmcts.reform.bulkscan.payment.processor.models.CreatePayment;
import uk.gov.hmcts.reform.bulkscan.payment.processor.models.PaymentInfo;

import java.util.Arrays;
import java.util.List;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentRequestMapperTest {

    @Mock
    private SiteConfiguration siteConfig;

    private PaymentRequestMapper paymentRequestMapper;

    public static final String PO_BOX = "BULKSCAN_PO_BOX";

    @BeforeEach
    void setUp() {
        paymentRequestMapper = new PaymentRequestMapper(siteConfig);
    }

    @Test
    void should_return_valid_PaymentRequest_when_CreatePayment_is_valid() {
        List<PaymentInfo> paymentInfos = Arrays.asList(
            new PaymentInfo("DCN1"),
            new PaymentInfo("DCN2")
        );
        CreatePayment createPayment = new CreatePayment(
            "envelope_id_123",
            "case_number_12345",
            false,
            PO_BOX,
            "Divorce",
            "finrem",
            paymentInfos
        );

        when(siteConfig.getSiteIdByPoBox(PO_BOX)).thenReturn("A123");

        CreatePaymentRequest expectedPaymentRequest = new CreatePaymentRequest(
            "case_number_12345",
            Arrays.asList("DCN1", "DCN2"),
            false,
            "A123"
        );

        CreatePaymentRequest paymentRequest = paymentRequestMapper.mapPayments(createPayment);
        assertThat(paymentRequest).usingRecursiveComparison().isEqualTo(expectedPaymentRequest);
    }

    @Test
    void should_throw_InvalidMessageException_when_no_payments_info_in_CreatePayment() {
        CreatePayment createPayment = new CreatePayment(
            "envelope_id_123",
            "case_number_12345",
            false,
            PO_BOX,
            "Divorce",
            "finrem",
            emptyList()
        );

        assertThatThrownBy(() -> paymentRequestMapper.mapPayments(createPayment))
            .isInstanceOf(PaymentDcnNotFoundException.class)
            .hasMessage("No Document Control Numbers found in the request. Envelope ID: envelope_id_123");
    }

    @Test
    void should_throw_SiteNotFoundException_when_site_not_found_for_poBox_in_CreatePayment() {
        List<PaymentInfo> paymentInfos = Arrays.asList(
            new PaymentInfo("DCN1")
        );
        CreatePayment createPayment = new CreatePayment(
            "envelope_id_123",
            "case_number_12345",
            false,
            PO_BOX,
            "Divorce",
            "finrem",
            paymentInfos
        );

        when(siteConfig.getSiteIdByPoBox(PO_BOX)).thenReturn(null);
        assertThatThrownBy(() -> paymentRequestMapper.mapPayments(createPayment))
            .isInstanceOf(SiteNotFoundException.class)
            .hasMessage("Site not Found for po box: " + PO_BOX);
    }
}
