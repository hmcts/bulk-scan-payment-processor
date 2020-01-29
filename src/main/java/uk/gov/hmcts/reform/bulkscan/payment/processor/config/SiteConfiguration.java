package uk.gov.hmcts.reform.bulkscan.payment.processor.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import uk.gov.hmcts.reform.bulkscan.payment.processor.exception.SiteConfigurationException;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@ConfigurationProperties(prefix = "site-mappings")
public class SiteConfiguration {
    private List<Site> sites;

    public List<Site> getSites() {
        return sites;
    }

    public void setSites(List<Site> sites) {
        if (sites == null || sites.isEmpty()) {
            throw new SiteConfigurationException("Site configuration missing");
        } else {
            this.sites = sites;
        }
    }

    public Optional<String> getSiteIdByPoBox(String poBox) {
        return sites
            .stream()
            .filter(s -> Objects.equals(s.poBox, poBox))
            .map(s -> s.siteId)
            .findFirst();
    }

    public static class Site {
        private String poBox;
        private String siteId;

        public Site(String poBox, String siteId) {
            this.poBox = poBox;
            this.siteId = siteId;
        }

        public Site() {
            // Spring needs it.
        }

        public String getPoBox() {
            return poBox;
        }

        public void setPoBox(String poBox) {
            this.poBox = poBox;
        }

        public String getSiteId() {
            return siteId;
        }

        public void setSiteId(String siteId) {
            this.siteId = siteId;
        }
    }
}
