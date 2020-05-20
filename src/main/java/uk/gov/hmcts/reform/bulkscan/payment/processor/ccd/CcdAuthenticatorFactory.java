package uk.gov.hmcts.reform.bulkscan.payment.processor.ccd;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.bulkscan.payment.processor.config.JurisdictionToUserMapping;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

@Service
@EnableConfigurationProperties(JurisdictionToUserMapping.class)
public class CcdAuthenticatorFactory {

    private final AuthTokenGenerator s2sTokenGenerator;
    private final IdamClient idamClient;
    private final JurisdictionToUserMapping users;

    public CcdAuthenticatorFactory(
        AuthTokenGenerator s2sTokenGenerator,
        IdamClient idamClient,
        JurisdictionToUserMapping users
    ) {
        this.s2sTokenGenerator = s2sTokenGenerator;
        this.idamClient = idamClient;
        this.users = users;
    }

    public CcdAuthenticator createForJurisdiction(String jurisdiction) {
        Credential user = users.getUser(jurisdiction);
        String userToken = idamClient.getAccessToken(user.getUsername(), user.getPassword());
        UserDetails userDetails = idamClient.getUserDetails(userToken);

        return new CcdAuthenticator(
            s2sTokenGenerator::generate,
            userDetails,
            () -> userToken
        );
    }
}
