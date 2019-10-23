package uk.gov.hmcts.reform.bulkscan.payment.processor;

import com.typesafe.config.ConfigFactory;
import io.restassured.RestAssured;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.reform.logging.appinsights.SyntheticHeaders;

import static org.hamcrest.Matchers.is;

@TestPropertySource("classpath:application.conf")
class PayHubApiHealthTest {

    private static final String TEST_PAY_HUB_URL = ConfigFactory.load().getString("test-pay-hub-url");

    @Test
    void pay_hub_health_check() {

        RestAssured
            .given()
            .baseUri(TEST_PAY_HUB_URL)
            .header(SyntheticHeaders.SYNTHETIC_TEST_SOURCE, "Bulk Scan Payment Processor smoke test")
            .get("/health")
            .then()
            .statusCode(200)
            .body("status", is("UP"));
    }

}
