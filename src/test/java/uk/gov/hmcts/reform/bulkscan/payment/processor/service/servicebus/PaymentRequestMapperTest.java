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
    public void should_return_valid_PaymentRequest_when_paymentMessage_is_valid() {

        when(siteConfig.getSiteIdByPoBox(PO_BOX)).thenReturn("A123");
        PaymentRequest expectedPaymentRequest = new PaymentRequest(
            "case_number_3333",
            ImmutableList.of(DCN_1, DCN_2),
            false,
            "A123"
        );

        PaymentRequest  paymentRequest= paymentRequestMapper.mapPaymentMessage(paymentMessage(
            "case_number_3333",
            false
        ));


        assertThat(paymentRequest).isEqualToComparingFieldByFieldRecursively(expectedPaymentRequest);
    }


    @Test
    public void should_throw_SiteNotConfiguredException_when_site_not_found() {

        when(siteConfig.getSiteIdByPoBox(PO_BOX)).thenReturn(null);

        assertThatThrownBy(
            () -> paymentRequestMapper.mapPaymentMessage(paymentMessage("case_number_1231", true)))
            .isInstanceOf(SiteNotFoundException.class)
            .hasMessage("Site not Found for  po box : "+PO_BOX);
    }


}
