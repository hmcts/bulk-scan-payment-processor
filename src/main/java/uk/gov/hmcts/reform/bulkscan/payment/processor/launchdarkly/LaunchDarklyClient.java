package uk.gov.hmcts.reform.bulkscan.payment.processor.launchdarkly;

import com.launchdarkly.sdk.LDUser;
import com.launchdarkly.sdk.server.interfaces.DataSourceStatusProvider;
import com.launchdarkly.sdk.server.interfaces.LDClientInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * LaunchDarkly client.
 */
@Service
public class LaunchDarklyClient {
    public static final LDUser BULK_SCAN_PAYMENT_PROCESSOR_USER = new LDUser.Builder("bulk-scan-payment-processor")
            .anonymous(true)
            .build();

    private final LDClientInterface internalClient;

    /**
     * Constructor for the LaunchDarklyClient.
     * @param launchDarklyClientFactory The factory for the LaunchDarkly client
     * @param sdkKey The SDK key
     * @param offlineMode The offline mode
     */
    @Autowired
    public LaunchDarklyClient(
        LaunchDarklyClientFactory launchDarklyClientFactory,
        @Value("${launchdarkly.sdk-key:YYYYY}") String sdkKey,
        @Value("${launchdarkly.offline-mode:false}") Boolean offlineMode
    ) {
        this.internalClient = launchDarklyClientFactory.create(sdkKey, offlineMode);
    }

    /**
     * Check if a feature is enabled using default user (bulk-scan-payment-processor).
     * @param feature The feature
     * @return True if the feature is enabled
     */
    public boolean isFeatureEnabled(String feature) {
        return internalClient.boolVariation(feature, LaunchDarklyClient.BULK_SCAN_PAYMENT_PROCESSOR_USER, false);
    }

    /**
     * Check if a feature is enabled using the given user.
     * @param feature The feature
     * @param user The user
     * @return True if the feature is enabled
     */
    public boolean isFeatureEnabled(String feature, LDUser user) {
        return internalClient.boolVariation(feature, user, false);
    }

    /**
     * Get the status of the data source.
     * @return The status of the data source
     */
    public DataSourceStatusProvider.Status getDataSourceStatus() {
        return internalClient.getDataSourceStatusProvider().getStatus();
    }
}
