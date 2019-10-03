package uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.bulkscan.payment.processor.client.payhub.request.PaymentRequest;
import uk.gov.hmcts.reform.bulkscan.payment.processor.config.SiteConfiguration;
import uk.gov.hmcts.reform.bulkscan.payment.processor.exception.SiteNotFoundException;
import uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.exceptions.InvalidMessageException;
import uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.model.PaymentMessage;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.bulkscan.payment.processor.data.producer.SamplePaymentMessageData.DCN_1;
import static uk.gov.hmcts.reform.bulkscan.payment.processor.data.producer.SamplePaymentMessageData.DCN_2;
import static uk.gov.hmcts.reform.bulkscan.payment.processor.data.producer.SamplePaymentMessageData.PO_BOX;
import static uk.gov.hmcts.reform.bulkscan.payment.processor.data.producer.SamplePaymentMessageData.paymentMessage;

@ExtendWith(MockitoExtension.class)
public class PaymentRequestMapperTest {

    @Mock
    private SiteConfiguration siteConfig;

    private PaymentRequestMapper paymentRequestMapper;

    @BeforeEach
    void setUp() {
        paymentRequestMapper = new PaymentRequestMapper(siteConfig);
    }

    @Test
    public void should_return_valid_PaymentRequest_when_PaymentMessage_is_valid() {
        // given
        when(siteConfig.getSiteIdByPoBox(PO_BOX)).thenReturn("A123");
        PaymentRequest expectedPaymentRequest = new PaymentRequest(
            "case_number_3333",
            ImmutableList.of(DCN_1, DCN_2),
            false,
            "A123"
        );

        // when
        PaymentRequest paymentRequest = paymentRequestMapper.mapPaymentMessage(paymentMessage(
            "case_number_3333",
            false
        ));

        // then
        assertThat(paymentRequest).isEqualToComparingFieldByFieldRecursively(expectedPaymentRequest);
    }

    @Test
    public void should_throw_exception_when_site_not_found_for_the_poBox() {
        // given
        when(siteConfig.getSiteIdByPoBox(PO_BOX)).thenReturn(null);

        // then
        assertThatThrownBy(
            () -> paymentRequestMapper.mapPaymentMessage(paymentMessage("case_number_1231", true)))
            .isInstanceOf(SiteNotFoundException.class)
            .hasMessage("Site not Found for po box: " + PO_BOX);
    }

    @Test
    public void should_throw_InvalidMessageException_when_no_payments_info() {
        // given
        PaymentMessage paymentMessage = new PaymentMessage(
            "envelope_id_123",
            "case_num_32213",
            true,
            PO_BOX,
            "Divorce",
            emptyList()
        );

        // then
        assertThatThrownBy(
            () -> paymentRequestMapper.mapPaymentMessage(paymentMessage))
            .isInstanceOf(InvalidMessageException.class)
            .hasMessage("No Document Control Numbers found in the payment message. MessageId: envelope_id_123");
    }
}
