package uk.gov.hmcts.reform.bulkscan.payment.processor.ccd;

import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.function.Supplier;

/**
 * Authenticator for CCD.
 */
public class CcdAuthenticator {

    private final UserDetails userDetails;
    private final Supplier<String> serviceTokenSupplier;
    private final Supplier<String> userTokenSupplier;

    /**
     * Constructor for the CcdAuthenticator.
     * @param serviceTokenSupplier The service token supplier
     * @param userDetails The user details
     * @param userTokenSupplier The user token supplier
     */
    public CcdAuthenticator(
        Supplier<String> serviceTokenSupplier,
        UserDetails userDetails,
        Supplier<String> userTokenSupplier
    ) {
        this.serviceTokenSupplier = serviceTokenSupplier;
        this.userDetails = userDetails;
        this.userTokenSupplier = userTokenSupplier;
    }

    /**
     * Get the user token.
     * @return The user token
     */
    public String getUserToken() {
        return this.userTokenSupplier.get();
    }

    /**
     * Get the service token.
     * @return The service token
     */
    public String getServiceToken() {
        return this.serviceTokenSupplier.get();
    }

    /**
     * Get the user details.
     * @return The user details
     */
    public UserDetails getUserDetails() {
        return this.userDetails;
    }
}
