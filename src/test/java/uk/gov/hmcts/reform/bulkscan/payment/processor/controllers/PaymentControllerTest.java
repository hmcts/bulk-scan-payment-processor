package uk.gov.hmcts.reform.bulkscan.payment.processor.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.bulkscan.payment.processor.models.CreatePayment;
import uk.gov.hmcts.reform.bulkscan.payment.processor.models.PaymentInfo;
import uk.gov.hmcts.reform.bulkscan.payment.processor.models.UpdatePayment;
import uk.gov.hmcts.reform.bulkscan.payment.processor.service.PaymentService;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PaymentController.class)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PaymentService paymentService;

    private CreatePayment validCreatePayment;
    private UpdatePayment validUpdatePayment;

    @BeforeEach
    void setUp() {
        PaymentInfo paymentInfo = new PaymentInfo("123456789");

        validCreatePayment = new CreatePayment(
            "envelope123",
            "ccd123",
            false,
            "poBox123",
            "jurisdiction123",
            "service123",
            List.of(paymentInfo)
        );

        validUpdatePayment = new UpdatePayment(
            "envelope123",
            "jurisdiction123",
            "exceptionRecord123",
            "newCase123"
        );
    }

    @Test
    void testCreatePayment_Success() throws Exception {
        doNothing().when(paymentService).createPayment(Mockito.any(CreatePayment.class));

        mockMvc.perform(post("/payment/create")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(asJsonString(validCreatePayment)))
            .andExpect(status().isCreated())
            .andExpect(content().string("Payment created successfully"));
    }

    @Test
    void testCreatePayment_MissingFields() throws Exception {
        CreatePayment invalidCreatePayment = new CreatePayment(
            "envelope123",
            "ccd123",
            false,
            "poBox123",
            "jurisdiction123",
            "service123",
            Collections.emptyList()
        );

        mockMvc.perform(post("/payment/create")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(asJsonString(invalidCreatePayment)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void testUpdatePayment_Success() throws Exception {
        doNothing().when(paymentService).updatePayment(Mockito.any(UpdatePayment.class));

        mockMvc.perform(post("/payment/update")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(asJsonString(validUpdatePayment)))
            .andExpect(status().isOk())
            .andExpect(content().string("Payment updated successfully"));
    }

    @Test
    void testUpdatePayment_MissingFields() throws Exception {
        UpdatePayment invalidUpdatePayment = new UpdatePayment(
            "envelope123",
            "jurisdiction123",
            "exceptionRecord123",
            ""
        );

        mockMvc.perform(post("/payment/update")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(asJsonString(invalidUpdatePayment)))
            .andExpect(status().isBadRequest());
    }

    private static String asJsonString(final Object obj) {
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
