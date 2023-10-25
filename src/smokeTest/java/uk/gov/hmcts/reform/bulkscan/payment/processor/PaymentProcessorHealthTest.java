package uk.gov.hmcts.reform.bulkscan.payment.processor;

import io.restassured.RestAssured;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Status;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.hamcrest.Matchers.equalTo;

@TestPropertySource("classpath:application.properties")
@ExtendWith(SpringExtension.class)
class PaymentProcessorHealthTest {

    @Value("${test-url}")
    private String testUrl;

    @Test
    void payment_processor_is_healthy() {
        RestAssured
            .given()
            .relaxedHTTPSValidation()
            .baseUri(testUrl)
            .get("/health")
            .then()
            .assertThat()
            .statusCode(HttpStatus.OK.value())
            .and()
            .body("status", equalTo(Status.UP.toString()));
    }
}
