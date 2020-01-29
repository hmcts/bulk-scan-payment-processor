package uk.gov.hmcts.reform.bulkscan.payment.processor.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@ConfigurationProperties(prefix = "site-mappings")
public class SiteConfiguration {
    private List<Sites> sites;

    public List<Sites> getSites() {
        return sites;
    }

    public void setSites(List<Sites> sites) {
        this.sites = sites;
    }

    public Optional<String> getSiteIdByPoBox(String poBox) {
        return sites
            .stream()
            .filter(s -> Objects.equals(s.poBox, poBox))
            .map(s -> s.siteId)
            .findFirst();
    }

    public static class Sites {
        private String poBox;
        private String siteId;

        public Sites(String poBox, String siteId) {
            this.poBox = poBox;
            this.siteId = siteId;
        }

        public Sites() {
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
