package uk.gov.hmcts.reform.bulkscan.payment.processor.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.bulkscan.payment.processor.ccd.Credential;
import uk.gov.hmcts.reform.bulkscan.payment.processor.exception.NoUserConfiguredException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

@ExtendWith(SpringExtension.class)
@Import(JurisdictionToUserMapping.class)
@EnableConfigurationProperties
@TestPropertySource(properties = {
    "idam.users.bulkscan.username=user@example.com",
    "idam.users.bulkscan.password=password1"
})
class JurisdictionToUserMappingTest {

    @Autowired
    private JurisdictionToUserMapping mapping;

    @Test
    void should_parse_up_the_properties_into_map() {
        Credential credentials = mapping.getUser("BULKSCAN");
        assertThat(credentials.getPassword()).isEqualTo("password1");
        assertThat(credentials.getUsername()).isEqualTo("user@example.com");
    }

    @Test
    void should_throw_exception_when_user_not_found() {
        Throwable throwable = catchThrowable(() -> mapping.getUser("nonexisting"));

        assertThat(throwable)
            .isInstanceOf(NoUserConfiguredException.class)
            .hasMessage("No user configured for jurisdiction: nonexisting");
    }

    @Test
    void should_throw_exception_if_none_configured() {
        Throwable throwable = catchThrowable(
            () -> new JurisdictionToUserMapping().getUser("NONE")
        );

        assertThat(throwable)
            .isInstanceOf(NoUserConfiguredException.class)
            .hasMessage("No user configured for jurisdiction: none");
    }
}
