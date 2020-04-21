package uk.gov.hmcts.reform.bulkscan.payment.processor.ccd;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.bulkscan.payment.processor.config.JurisdictionToUserMapping;
import uk.gov.hmcts.reform.bulkscan.payment.processor.exception.NoUserConfiguredException;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class CcdAuthenticatorFactoryTest {

    @Mock
    private AuthTokenGenerator tokenGenerator;
    @Mock
    private IdamClient idamClient;
    @Mock
    private JurisdictionToUserMapping users;

    private CcdAuthenticatorFactory service;

    @BeforeEach
    void before() {
        service = new CcdAuthenticatorFactory(tokenGenerator, idamClient, users);
    }

    @Test
    void should_successfully_return_authInfo() {
        // given
        String jurisdiction = "jurisdiction1";
        String serviceToken = "serviceToken1";
        Credential credentials = mock(Credential.class);
        String userToken = "userToken1";
        UserDetails userDetails = mock(UserDetails.class);

        given(users.getUser(eq(jurisdiction))).willReturn(credentials);
        given(tokenGenerator.generate()).willReturn(serviceToken);
        given(idamClient.getAccessToken(any(), any())).willReturn(userToken);
        given(idamClient.getUserDetails(userToken)).willReturn(userDetails);

        // when
        CcdAuthenticator authenticator = service.createForJurisdiction(jurisdiction);

        // then
        assertThat(authenticator.getServiceToken()).isEqualTo(serviceToken);
        assertThat(authenticator.getUserToken()).isEqualTo(userToken);
        assertThat(authenticator.getUserDetails()).isEqualTo(userDetails);
    }

    @Test
    void should_throw_exception_when_jurisdiction_to_user_mapping_fails() {
        String jurisdiction = "jurisdiction1";
        Exception mappingException = new NoUserConfiguredException(jurisdiction);

        willThrow(mappingException).given(users).getUser(jurisdiction);

        assertThatThrownBy(
            () -> service.createForJurisdiction(jurisdiction)
        ).isSameAs(mappingException);
    }
}
