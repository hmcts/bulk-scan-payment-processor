package uk.gov.hmcts.reform.bulkscan.payment.processor.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import uk.gov.hmcts.reform.bulkscan.payment.processor.ccd.Credential;
import uk.gov.hmcts.reform.bulkscan.payment.processor.exception.NoUserConfiguredException;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static java.util.Map.Entry;
import static java.util.stream.Collectors.toMap;

/**
 * Configuration for mapping jurisdictions to users.
 */
@ConfigurationProperties(prefix = "idam")
public class JurisdictionToUserMapping {

    private Map<String, Credential> users = new HashMap<>();

    /**
     * Set the users.
     * @param users The users
     */
    public void setUsers(Map<String, Map<String, String>> users) {
        this.users = users
            .entrySet()
            .stream()
            .map(this::createEntry)
            .collect(toMap(Entry::getKey, Entry::getValue));
    }

    /**
     * Get the users.
     * @return The users
     */
    public Map<String, Credential> getUsers() {
        return users;
    }

    /**
     * Create credential from entry.
     * @param entry The entry
     */
    private Entry<String, Credential> createEntry(Entry<String, Map<String, String>> entry) {
        String key = entry.getKey().toLowerCase(Locale.getDefault());
        Credential cred = new Credential(entry.getValue().get("username"), entry.getValue().get("password"));

        return new AbstractMap.SimpleEntry<>(key, cred);
    }

    /**
     * Get the user for a jurisdiction.
     * @param jurisdiction The jurisdiction
     * @return The user
     */
    public Credential getUser(String jurisdiction) {
        return users.computeIfAbsent(jurisdiction.toLowerCase(), this::throwNotFound);
    }

    /**
     * Throw a not found exception.
     * @param jurisdiction The jurisdiction
     */
    private Credential throwNotFound(String jurisdiction) {
        throw new NoUserConfiguredException(jurisdiction);
    }
}
