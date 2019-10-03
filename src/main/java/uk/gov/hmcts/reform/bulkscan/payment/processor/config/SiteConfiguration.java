package uk.gov.hmcts.reform.bulkscan.payment.processor.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import uk.gov.hmcts.reform.bulkscan.payment.processor.exception.SiteConfigurationException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;

@ConfigurationProperties(prefix = "site-mappings")
public class SiteConfiguration {
    private List<Sites> sites;

    private Map<String, String> poBoxToSiteIdMap;

    public List<Sites> getSites() {
        return sites;
    }

    public void setSites(List<Sites> sites) {
        this.sites = sites;
    }

    @PostConstruct
    private void mapPoBoxToSiteId() {
        if (getSites() == null || getSites().isEmpty()) {
            throw new SiteConfigurationException("Site configuration missing");
        }
        poBoxToSiteIdMap = getSites()
            .stream()
            .collect(Collectors.toMap(Sites::getPoBox, Sites::getSiteId));
    }

    public String getSiteIdByPoBox(String poBox) {
        return poBoxToSiteIdMap.get(poBox);
    }

    public static class Sites {

        private String siteName;
        private String poBox;
        private String siteId;

        public Sites(String siteName, String poBox, String siteId) {
            this.siteName = siteName;
            this.poBox = poBox;
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
