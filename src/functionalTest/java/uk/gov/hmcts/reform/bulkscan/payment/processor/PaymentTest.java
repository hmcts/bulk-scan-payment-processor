package uk.gov.hmcts.reform.bulkscan.payment.processor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.bulkscan.payment.processor.models.CreatePayment;
import uk.gov.hmcts.reform.bulkscan.payment.processor.models.PaymentInfo;
import uk.gov.hmcts.reform.bulkscan.payment.processor.models.UpdatePayment;

import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("functional")
public class PaymentTest {

    private static MockWebServer payHubMockWebServer;

    @Autowired
    private ObjectMapper objectMapper;

    @LocalServerPort
    private int port;

    private static final String CREATE_PAYMENT_ENDPOINT = "/payment/create";
    private static final String UPDATE_PAYMENT_ENDPOINT = "/payment/update";

    private static final String ERR_ENVELOPE_ID = "Envelope ID is required";
    private static final String ERR_CCD_REF = "CCD Reference is required";
    private static final String ERR_PO_BOX = "PO Box is required";
    private static final String ERR_JURISDICTION = "Jurisdiction is required";
    private static final String ERR_SERVICE = "Service is required";
    private static final String ERR_PAYMENTS_LIST = "Payments list is required";
    private static final String ERR_EXCEPTION_RECORD_REF = "Exception record reference is required";
    private static final String ERR_NEW_CASE_REF = "New case reference is required";

    @BeforeAll
    static void startMockServers() throws IOException {
        payHubMockWebServer = new MockWebServer();
        payHubMockWebServer.start(8082);
    }

    @AfterAll
    static void stopMockServers() throws IOException {
        if (payHubMockWebServer != null) {
            payHubMockWebServer.shutdown();
        }
    }

    @BeforeEach
    void setupRestAssured() {
        RestAssured.port = port;
    }

    private ValidatableResponse postPayment(String endpoint, Object body) throws JsonProcessingException {
        return RestAssured.given()
            .contentType(ContentType.JSON)
            .body(objectMapper.writeValueAsString(body))
            .when()
            .post(endpoint)
            .then();
    }

    @Test
    void createPayment_HappyPath() throws Exception {
        payHubMockWebServer.enqueue(new MockResponse()
                                        .setResponseCode(200)
                                        .addHeader("Content-Type", "application/json")
                                        .setBody("{\"payment_dcns\": [\"123456789012345678901\"]}"));

        CreatePayment createPayment = new CreatePayment(
            "550e8400-e29b-41d4-a716-446655440000",
            "1234567890123456",
            false,
            "12625",
            "probate",
            "probate",
            List.of(new PaymentInfo("123456789012345678901"))
        );

        postPayment(CREATE_PAYMENT_ENDPOINT, createPayment)
            .statusCode(201)
            .body(equalTo("Payment created successfully"));
    }

    @Test
    void createPayment_PayHub_Failure() throws Exception {
        payHubMockWebServer.enqueue(new MockResponse()
                                        .setResponseCode(400)
                                        .addHeader("Content-Type", "application/json")
                                        .setBody("{\"error\":\"Bad Request\"}"));

        CreatePayment createPayment = new CreatePayment(
            "550e8400-e29b-41d4-a716-446655441200",
            "1234567890123457",
            false,
            "12625",
            "probate",
            "probate",
            List.of(new PaymentInfo("123456789012343678901"))
        );

        postPayment(CREATE_PAYMENT_ENDPOINT, createPayment)
            .statusCode(424)
            .body(containsString("Failed creating payment."));
    }

    @ParameterizedTest
    @MethodSource("createPaymentValidationProvider")
    void createPaymentValidationErrors(CreatePayment input, String expectedError) throws Exception {
        postPayment(CREATE_PAYMENT_ENDPOINT, input)
            .statusCode(400)
            .body(containsString(expectedError));
    }

    static Stream<Arguments> createPaymentValidationProvider() {
        return Stream.of(
            Arguments.of(
                new CreatePayment(
                    null,
                    "1234567890123456",
                    false,
                    "12625",
                    "probate",
                    "probate",
                    List.of(new PaymentInfo("123456789012345678901"))
                ),
                ERR_ENVELOPE_ID
            ),
            Arguments.of(
                new CreatePayment(
                    "id",
                    null,
                    false,
                    "12625",
                    "probate",
                    "probate",
                    List.of(new PaymentInfo("123456789012345678901"))
                ),
                ERR_CCD_REF
            ),
            Arguments.of(
                new CreatePayment(
                    "id",
                    "1234567890123456",
                    false,
                    null,
                    "probate",
                    "probate",
                    List.of(new PaymentInfo("123456789012345678901"))
                ),
                ERR_PO_BOX
            ),
            Arguments.of(
                new CreatePayment(
                    "id",
                    "1234567890123456",
                    false,
                    "12625",
                    null,
                    "probate",
                    List.of(new PaymentInfo("123456789012345678901"))
                ),
                ERR_JURISDICTION
            ),
            Arguments.of(
                new CreatePayment(
                    "id",
                    "1234567890123456",
                    false,
                    "12625",
                    "probate",
                    null,
                    List.of(new PaymentInfo("123456789012345678901"))
                ),
                ERR_SERVICE
            ),
            Arguments.of(
                new CreatePayment(
                    "id",
                    "1234567890123456",
                    false,
                    "12625",
                    "probate",
                    "probate",
                    null
                ),
                ERR_PAYMENTS_LIST
            )
        );
    }

    @Test
    void updatePayment_HappyPath() throws Exception {
        payHubMockWebServer.enqueue(new MockResponse()
                                        .setResponseCode(200)
                                        .addHeader("Content-Type", "application/json")
                                        .setBody("{\"status\": \"success\"}"));

        UpdatePayment updatePayment = new UpdatePayment(
            "550e8400-e29b-41d4-a716-446655440000",
            "probate",
            "EXC1234567890",
            "NEW1234567890"
        );

        postPayment(UPDATE_PAYMENT_ENDPOINT, updatePayment)
            .statusCode(200)
            .body(equalTo("Payment updated successfully"));
    }

    @Test
    void updatePayment_PayHub_Failure() throws Exception {
        payHubMockWebServer.enqueue(new MockResponse()
                                        .setResponseCode(400)
                                        .addHeader("Content-Type", "application/json")
                                        .setBody("{\"error\": \"Bad Request\"}"));

        UpdatePayment updatePayment = new UpdatePayment(
            "550e8400-e29b-41d4-a716-446655440000",
            "probate",
            "EXC1234567890",
            "NEW1234567890"
        );

        postPayment(UPDATE_PAYMENT_ENDPOINT, updatePayment)
            .statusCode(424)
            .body(containsString("Failed updating payment."));
    }

    @ParameterizedTest
    @MethodSource("updatePaymentValidationProvider")
    void updatePaymentValidationErrors(UpdatePayment input, String expectedError) throws Exception {
        postPayment(UPDATE_PAYMENT_ENDPOINT, input)
            .statusCode(400)
            .body(containsString(expectedError));
    }

    static Stream<Arguments> updatePaymentValidationProvider() {
        return Stream.of(
            Arguments.of(
                new UpdatePayment(
                    null,
                    "probate",
                    "EXC1234567890",
                    "NEW1234567890"
                ),
                ERR_ENVELOPE_ID
            ),
            Arguments.of(
                new UpdatePayment(
                    "id",
                    null,
                    "EXC1234567890",
                    "NEW1234567890"
                ),
                ERR_JURISDICTION
            ),
            Arguments.of(
                new UpdatePayment(
                    "id",
                    "probate",
                    null,
                    "NEW1234567890"
                ),
                ERR_EXCEPTION_RECORD_REF
            ),
            Arguments.of(
                new UpdatePayment(
                    "id",
                    "probate",
                    "EXC1234567890",
                    null
                ),
                ERR_NEW_CASE_REF
            )
        );
    }
}
