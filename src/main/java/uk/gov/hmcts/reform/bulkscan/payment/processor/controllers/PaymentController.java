package uk.gov.hmcts.reform.bulkscan.payment.processor.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.bulkscan.payment.processor.errorhandling.exception.PayHubCallException;
import uk.gov.hmcts.reform.bulkscan.payment.processor.models.CreatePayment;
import uk.gov.hmcts.reform.bulkscan.payment.processor.models.UpdatePayment;
import uk.gov.hmcts.reform.bulkscan.payment.processor.service.PaymentService;

import static java.lang.String.format;

@RestController
@RequestMapping("/payment")
@Profile("!functional")
@Tag(name = "Payment API", description = "API for managing payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/create")
    @Operation(
        summary = "Create a new payment",
        description = "Creates a new payment based on the provided details"
    )
    @ApiResponse(responseCode = "201", description = "Payment has been created")
    @ApiResponse(responseCode = "400", description = "Missing required fields")
    @ApiResponse(responseCode = "424", description = "Error back from payment hub")
    @ApiResponse(responseCode = "424", description = "Error back from CCD")
    public ResponseEntity<String> createPayment(@Valid @RequestBody CreatePayment createPayment) {
        throw new PayHubCallException(
            format(
                "Failed creating payment. Envelope ID: %s",
                createPayment.getEnvelopeId()
            ),
            new RuntimeException("Payment failed")
        );
    }

    @PostMapping("/update")
    @Operation(
        summary = "Update an existing payment",
        description = "Updates an existing payment based on the provided details"
    )
    @ApiResponse(responseCode = "200", description = "Payment has been updated")
    @ApiResponse(responseCode = "400", description = "Missing required fields")
    @ApiResponse(responseCode = "424", description = "Error back from payment hub")
    @ApiResponse(responseCode = "424", description = "Error back from CCD")
    public ResponseEntity<String> updatePayment(@Valid @RequestBody UpdatePayment updatePayment) {
        throw new PayHubCallException(
            format(
                "Failed creating payment. Envelope ID: %s",
                updatePayment.getEnvelopeId()
            ),
            new RuntimeException("Payment failed")
        );
    }
}

