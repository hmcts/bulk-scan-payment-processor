package uk.gov.hmcts.reform.bulkscan.payment.processor.service;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.bulkscan.payment.processor.client.payhub.request.CreatePaymentRequest;
import uk.gov.hmcts.reform.bulkscan.payment.processor.config.SiteConfiguration;
import uk.gov.hmcts.reform.bulkscan.payment.processor.errorhandling.exception.SiteNotFoundException;
import uk.gov.hmcts.reform.bulkscan.payment.processor.models.CreatePayment;
import uk.gov.hmcts.reform.bulkscan.payment.processor.models.PaymentInfo;
import uk.gov.hmcts.reform.bulkscan.payment.processor.service.exceptions.InvalidMessageException;
import uk.gov.hmcts.reform.bulkscan.payment.processor.service.model.CreatePaymentMessage;

import java.util.Arrays;
import java.util.List;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.bulkscan.payment.processor.data.producer.SamplePaymentMessageData.DCN_1;
import static uk.gov.hmcts.reform.bulkscan.payment.processor.data.producer.SamplePaymentMessageData.DCN_2;
import static uk.gov.hmcts.reform.bulkscan.payment.processor.data.producer.SamplePaymentMessageData.PO_BOX;
import static uk.gov.hmcts.reform.bulkscan.payment.processor.data.producer.SamplePaymentMessageData.paymentMessage;

@ExtendWith(MockitoExtension.class)
class PaymentRequestMapperTest {

    @Mock
    private SiteConfiguration siteConfig;

    private PaymentRequestMapper paymentRequestMapper;

    @BeforeEach
    void setUp() {
        paymentRequestMapper = new PaymentRequestMapper(siteConfig);
    }

    @Test
    void should_return_valid_PaymentRequest_when_PaymentMessage_is_valid() {
        // given
        when(siteConfig.getSiteIdByPoBox(PO_BOX)).thenReturn("A123");
        CreatePaymentRequest expectedPaymentRequest = new CreatePaymentRequest(
            "case_number_3333",
            ImmutableList.of(DCN_1, DCN_2),
            false,
            "A123"
        );

        // when
        CreatePaymentRequest paymentRequest = paymentRequestMapper.mapPaymentMessage(paymentMessage(
            "case_number_3333",
            false,
            "CREATE"
        ));

        // then
        assertThat(paymentRequest).usingRecursiveComparison().isEqualTo(expectedPaymentRequest);
    }

    @Test
    void should_throw_exception_when_site_not_found_for_the_poBox() {
        // given
        when(siteConfig.getSiteIdByPoBox(PO_BOX)).thenReturn(null);

        CreatePaymentMessage paymentMessage = paymentMessage("case_number_1231", true, "CREATE");

        // then
        assertThatThrownBy(() -> paymentRequestMapper.mapPaymentMessage(paymentMessage))
            .isInstanceOf(SiteNotFoundException.class)
            .hasMessage("Site not Found for po box: " + PO_BOX);
    }

    @Test
    void should_throw_InvalidMessageException_when_no_payments_info() {
        // given
        CreatePaymentMessage paymentMessage = new CreatePaymentMessage(
            "CREATE",
            "envelope_id_123",
            "case_num_32213",
            true,
            PO_BOX,
            "Divorce",
            "finrem",
            emptyList()
        );

        // then
        assertThatThrownBy(
            () -> paymentRequestMapper.mapPaymentMessage(paymentMessage))
            .isInstanceOf(InvalidMessageException.class)
            .hasMessage("No Document Control Numbers found in the payment message. MessageId: envelope_id_123");
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
            .isInstanceOf(InvalidMessageException.class)
            .hasMessage("No Document Control Numbers found in the payment message. MessageId: envelope_id_123");
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
