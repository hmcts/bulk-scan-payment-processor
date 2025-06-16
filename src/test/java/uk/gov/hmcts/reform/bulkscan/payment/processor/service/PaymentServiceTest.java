package uk.gov.hmcts.reform.bulkscan.payment.processor.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.bulkscan.payment.processor.client.processor.ProcessorClient;
import uk.gov.hmcts.reform.bulkscan.payment.processor.models.CreatePayment;
import uk.gov.hmcts.reform.bulkscan.payment.processor.models.PaymentInfo;
import uk.gov.hmcts.reform.bulkscan.payment.processor.models.UpdatePayment;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentHubHandlerService paymentHubHandlerService;

    @Mock
    private ProcessorClient processorClient;

    @InjectMocks
    private PaymentService paymentService;

    private CreatePayment createValidCreatePayment() {
        PaymentInfo paymentInfo = new PaymentInfo("123456789");
        return new CreatePayment(
            "envelope123",
            "ccd123",
            false,
            "poBox123",
            "jurisdiction123",
            "service123",
            List.of(paymentInfo)
        );
    }

    private UpdatePayment createValidUpdatePayment() {
        return new UpdatePayment(
            "envelope123",
            "jurisdiction123",
            "exceptionRecord123",
            "newCase123"
        );
    }

    @Test
    void testCreatePayment_Success() {
        CreatePayment createPayment = createValidCreatePayment();

        paymentService.createPayment(createPayment);

        verify(paymentHubHandlerService, times(1)).handleCreatingPayment(createPayment);
        verify(processorClient, times(1)).updatePayments(createPayment.getPayments());
    }

    @Test
    void testCreatePayment_PaymentHubHandlerServiceException() {
        CreatePayment createPayment = createValidCreatePayment();

        doThrow(new RuntimeException("PaymentHubHandlerService failed"))
            .when(paymentHubHandlerService).handleCreatingPayment(createPayment);

        assertThrows(RuntimeException.class, () -> paymentService.createPayment(createPayment));

        verify(paymentHubHandlerService, times(1)).handleCreatingPayment(createPayment);
        verify(processorClient, times(0)).updatePayments(anyList());
    }

    @Test
    void testUpdatePayment_Success() {
        UpdatePayment updatePayment = createValidUpdatePayment();

        paymentService.updatePayment(updatePayment);

        verify(paymentHubHandlerService, times(1)).updatePaymentCaseReference(updatePayment);
    }

    @Test
    void testUpdatePayment_PaymentHubHandlerServiceException() {
        UpdatePayment updatePayment = createValidUpdatePayment();

        doThrow(new RuntimeException("PaymentHubHandlerService failed"))
            .when(paymentHubHandlerService).updatePaymentCaseReference(updatePayment);

        assertThrows(RuntimeException.class, () -> paymentService.updatePayment(updatePayment));

        verify(paymentHubHandlerService, times(1)).updatePaymentCaseReference(updatePayment);
    }
}
