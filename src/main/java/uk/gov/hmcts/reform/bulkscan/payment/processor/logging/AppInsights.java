package uk.gov.hmcts.reform.bulkscan.payment.processor.logging;

import com.microsoft.applicationinsights.TelemetryClient;
import com.microsoft.azure.servicebus.IMessage;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.function.Supplier;

import static java.util.Map.entry;

@Component
public class AppInsights {
    protected static final String PAYMENT = "Payment";
    private final TelemetryClient telemetryClient;

    public AppInsights(TelemetryClient telemetryClient) {
        this.telemetryClient = telemetryClient;
    }

    public void tracePaymentFailure(IMessage message, String reason, Supplier<String>  description,
                                    Supplier<String> envelopeId, Supplier<String> jurisdiction,
                                    Supplier<String> exceptionRecordRef,
                                    Supplier<String> ccdCaseNumber) {

        Map<String, String> properties
            = Map.ofEntries(entry("messageId", message.getMessageId()),
                            entry("reason", reason),
                            entry("description", description.get()),
                            entry("envelopeId", envelopeId.get()),
                            entry("jurisdiction", jurisdiction.get()),
                            entry("exceptionRecordRef", exceptionRecordRef.get()),
                            entry("ccdCaseNumber", ccdCaseNumber.get()));

        Map<String, Double> metrix = Map.ofEntries(entry(
            "deliveryCount", (double) (message.getDeliveryCount() + 1)));// starts from 0)

        telemetryClient.trackEvent(PAYMENT, properties, metrix);
    }
}
