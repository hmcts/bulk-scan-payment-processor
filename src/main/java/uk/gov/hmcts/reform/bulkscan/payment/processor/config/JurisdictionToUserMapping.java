package uk.gov.hmcts.reform.bulkscan.payment.processor.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import uk.gov.hmcts.reform.bulkscan.payment.processor.ccd.Credential;
import uk.gov.hmcts.reform.bulkscan.payment.processor.exception.NoUserConfiguredException;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static java.util.Map.Entry;
import static java.util.stream.Collectors.toMap;

@ConfigurationProperties(prefix = "idam")
public class JurisdictionToUserMapping {

    private static final Logger log = LoggerFactory.getLogger(JurisdictionToUserMapping.class);

    private Map<String, Credential> users = new HashMap<>();

    public void setUsers(Map<String, Map<String, String>> users) {
        this.users = users
            .entrySet()
            .stream()
            .map(this::createEntry)
            .collect(toMap(Entry::getKey, Entry::getValue));
    }

    public Map<String, Credential> getUsers() {
        return users;
    }

    private Entry<String, Credential> createEntry(Entry<String, Map<String, String>> entry) {
        String key = entry.getKey().toLowerCase(Locale.getDefault());
        Credential cred = new Credential(entry.getValue().get("username"), entry.getValue().get("password"));

        return new AbstractMap.SimpleEntry<>(key, cred);
    }

    public Credential getUser(String jurisdiction) {
        users.entrySet().stream()
            .forEach(e -> log.info("user details: " + e.getKey() + ": " + e.getValue().getUsername()));
        return users.computeIfAbsent(jurisdiction.toLowerCase(), this::throwNotFound);
    }

    private Credential throwNotFound(String jurisdiction) {
        throw new NoUserConfiguredException(jurisdiction);
    }
}
