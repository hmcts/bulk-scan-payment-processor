package uk.gov.hmcts.reform.bulkscan.payment.processor.logging;

import com.microsoft.applicationinsights.TelemetryClient;
import com.microsoft.azure.servicebus.IMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.bulkscan.payment.processor.logging.AppInsights.PAYMENT;

@ExtendWith(MockitoExtension.class)
class AppInsightsTest {
    private AppInsights appInsights;

    @Mock
    private TelemetryClient telemetryClient;

    @Mock
    private IMessage message;

    @Captor
    private ArgumentCaptor<Map<String, String>> propertiesCap;
    @Captor
    private ArgumentCaptor<Map<String, Double>> metrixCap;


    @BeforeEach
    void setUp() {
        appInsights = new AppInsights(telemetryClient);
    }

    @Test
    void should_invoke_telemetry_with_all_values() {
        String messageId = "12345";
        String reason = "Payment Message processing error";
        String description = "JsonParseException";
        String envelopId = "env-12312";
        String jurisdiction = "PROBATE";
        String exceptionRecordRef = "excp-ref-9999";
        String ccdCaseNumber = "new-case-ref-12312";
        given(message.getMessageId()).willReturn(messageId);
        given(message.getDeliveryCount()).willReturn(2L);
        appInsights.tracePaymentFailure(message, reason, () -> description, () -> envelopId, () -> jurisdiction,
            () -> exceptionRecordRef, () -> ccdCaseNumber);

        verify(telemetryClient).trackEvent(same(PAYMENT), propertiesCap.capture(), metrixCap.capture());

        Map<String, String> properties = propertiesCap.getValue();
        assertThat(properties.get("messageId")).isEqualTo(messageId);
        assertThat(properties.get("reason")).isEqualTo(reason);
        assertThat(properties.get("envelopeId")).isEqualTo(envelopId);
        assertThat(properties.get("jurisdiction")).isEqualTo(jurisdiction);
        assertThat(properties.get("exceptionRecordRef")).isEqualTo(exceptionRecordRef);
        assertThat(properties.get("ccdCaseNumber")).isEqualTo(ccdCaseNumber);

        Map<String, Double> metrix = metrixCap.getValue();
        assertThat(metrix.get("deliveryCount")).isEqualTo(3);
    }
}
