package uk.gov.hmcts.reform.bulkscan.payment.processor.config;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.bulkscan.payment.processor.exception.SiteConfigurationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


public class SiteConfigurationTest {

    @Test
    public void should_map_poBox_to_siteId() {
        SiteConfiguration siteConfiguration = new SiteConfiguration();
        siteConfiguration.setSites(
            ImmutableList.of(
                new SiteConfiguration.Site("12345", "99"),
                new SiteConfiguration.Site("11111", "11"),
                new SiteConfiguration.Site("98756", "44")
            )
        );

        assertThat(siteConfiguration.getSiteIdByPoBox("12345")).contains("99");
        assertThat(siteConfiguration.getSiteIdByPoBox("11111")).contains("11");
        assertThat(siteConfiguration.getSiteIdByPoBox("98756")).contains("44");
        assertThat(siteConfiguration.getSiteIdByPoBox("80000")).isEmpty();
    }

    @Test
    public void should_throw_exception_when_sites_config_empty() {
        var siteConfiguration = new SiteConfiguration();
        assertThatThrownBy(() -> siteConfiguration.setSites(null))
            .isInstanceOf(SiteConfigurationException.class)
            .hasMessage("Site configuration missing");
    }
}
