package uk.gov.hmcts.reform.bulkscan.payment.processor;

import io.restassured.RestAssured;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Status;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestPropertySource;

import static org.hamcrest.Matchers.equalTo;

@TestPropertySource("classpath:application.properties")
class PaymentProcessorHealthTest {

    @Value("${test-url}")
    private String TEST_URL;

    @Test
    void payment_processor_is_healthy() {
        RestAssured
            .given()
            .relaxedHTTPSValidation()
            .baseUri(TEST_URL)
            .get("/health")
            .then()
            .assertThat()
            .statusCode(HttpStatus.OK.value())
            .and()
            .body("status", equalTo(Status.UP.toString()));
    }
}
