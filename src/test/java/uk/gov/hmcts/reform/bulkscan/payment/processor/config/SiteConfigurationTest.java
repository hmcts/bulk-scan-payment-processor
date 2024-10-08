package uk.gov.hmcts.reform.bulkscan.payment.processor.config;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.bulkscan.payment.processor.errorhandling.exception.SiteConfigurationException;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SiteConfigurationTest {

    private final SiteConfiguration siteConfiguration = new SiteConfiguration();

    @Test
    void should_map_poBox_to_siteId() {

        siteConfiguration.setSites(
            ImmutableList.of(
                new SiteConfiguration.Sites("site_a", singletonList("12345"), "99"),
                new SiteConfiguration.Sites("site_b", asList("11111", "22222"), "11"),
                new SiteConfiguration.Sites("site_c", singletonList("98756"), "44")
            )
        );

        ReflectionTestUtils.invokeMethod(siteConfiguration, "mapPoBoxToSiteId");

        assertThat(siteConfiguration.getSiteIdByPoBox("12345")).isEqualTo("99");
        assertThat(siteConfiguration.getSiteIdByPoBox("11111")).isEqualTo("11");
        assertThat(siteConfiguration.getSiteIdByPoBox("22222")).isEqualTo("11");
        assertThat(siteConfiguration.getSiteIdByPoBox("98756")).isEqualTo("44");
        assertThat(siteConfiguration.getSiteIdByPoBox("80000")).isNull();

    }

    @Test
    void should_throw_exception_when_sites_config_empty() {

        assertThatThrownBy(() -> ReflectionTestUtils.invokeMethod(siteConfiguration, "mapPoBoxToSiteId"))
            .isInstanceOf(SiteConfigurationException.class)
            .hasMessage("Site configuration missing");
    }
}
