package uk.gov.hmcts.reform.bulkscan.payment.processor;

import com.typesafe.config.ConfigFactory;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@TestPropertySource("classpath:application.conf")
public class DummySmokeTest {

    private static final String TEST_URL = ConfigFactory.load().getString("test-url");


    @Test
    public void replaceThisWithActualTests() {
        // TODO: this test is there so that a test report can be created for smoke tests
        // (otherwise the build fails). Remove when actual smoke tests have been written.
        Response response = RestAssured
            .given()
            .relaxedHTTPSValidation()
            .baseUri(TEST_URL)
            .get("/health")
            .andReturn();

        assertThat(response.getStatusCode()).isEqualTo(200);
    }
}
