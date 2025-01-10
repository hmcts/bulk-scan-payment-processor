package uk.gov.hmcts.reform.bulkscan.payment.processor.config;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bulkscan.payment.processor.errorhandling.exception.SiteConfigurationException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Configuration for site mappings.
 */
@Component
@ConfigurationProperties(prefix = "site-mappings")
public class SiteConfiguration {
    private List<Sites> sites;

    private Map<String, String> poBoxToSiteIdMap;

    /**
     * Get the sites.
     * @return The sites
     */
    public List<Sites> getSites() {
        return sites;
    }

    /**
     * Set the sites.
     * @param sites The sites
     */
    public void setSites(List<Sites> sites) {
        this.sites = sites;
    }

    /**
     * Map po box to site id.
     */
    @PostConstruct
    private void mapPoBoxToSiteId() {
        if (getSites() == null || getSites().isEmpty()) {
            throw new SiteConfigurationException("Site configuration missing");
        }
        poBoxToSiteIdMap = getSites()
            .stream()
                .reduce(
                        new HashMap<>(),
                        (m, s) -> {
                            for (String poBox: s.poBoxes) {
                                m.put(poBox, s.siteId);
                            }
                            return m;
                        },
                        (m1, m2) -> {
                            m1.putAll(m2);
                            return m1;
                        }
                );
    }

    public String getSiteIdByPoBox(String poBox) {
        return poBoxToSiteIdMap.get(poBox);
    }

    public static class Sites {

        private String siteName;
        private List<String> poBoxes;
        private String siteId;

        public Sites(String siteName, List<String> poBoxes, String siteId) {
            this.siteName = siteName;
            this.poBoxes = poBoxes;
            this.siteId = siteId;
        }

        public Sites() {
            // Spring needs it.
        }

        public String getSiteName() {
            return siteName;
        }

        public void setSiteName(String siteName) {
            this.siteName = siteName;
        }

        public List<String> getPoBoxes() {
            return poBoxes;
        }

        public void setPoBoxes(List<String> poBoxes) {
            this.poBoxes = poBoxes;
        }

        public String getSiteId() {
            return siteId;
        }

        public void setSiteId(String siteId) {
            this.siteId = siteId;
        }
    }
}
