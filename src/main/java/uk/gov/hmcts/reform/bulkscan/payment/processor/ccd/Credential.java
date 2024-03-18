package uk.gov.hmcts.reform.bulkscan.payment.processor.ccd;

/**
 * Represents the credentials for the CCD API.
 */
public class Credential {
    private final String username;
    private final String password;

    /**
     * Constructor for the Credential.
     * @param username The username
     * @param password The password
     */
    public Credential(String username, String password) {
        this.username = username;
        this.password = password;
    }

    /**
     * Get the username.
     * @return The username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Get the password.
     * @return The password
     */
    public String getPassword() {
        return password;
    }
}
