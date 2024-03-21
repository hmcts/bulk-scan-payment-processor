package uk.gov.hmcts.reform.bulkscan.payment.processor.launchdarkly;

import com.launchdarkly.sdk.server.LDClient;
import com.launchdarkly.sdk.server.LDConfig;
import com.launchdarkly.sdk.server.interfaces.LDClientInterface;
import org.springframework.stereotype.Service;

/**
 * Factory for creating LaunchDarkly clients.
 */
@Service
public class LaunchDarklyClientFactory {

    /**
     * Create a new LaunchDarkly client.
     * @param sdkKey The SDK key
     * @param offlineMode The offline mode
     * @return The LaunchDarkly client
     */
    public LDClientInterface create(String sdkKey, boolean offlineMode) {
        LDConfig config = new LDConfig.Builder()
            .offline(offlineMode)
            .build();
        return new LDClient(sdkKey, config);
    }
}
