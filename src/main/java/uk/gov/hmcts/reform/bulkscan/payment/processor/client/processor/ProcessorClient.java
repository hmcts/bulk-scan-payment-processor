package uk.gov.hmcts.reform.bulkscan.payment.processor.client.processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.bulkscan.payment.processor.client.processor.request.PaymentRequest;
import uk.gov.hmcts.reform.bulkscan.payment.processor.client.processor.response.PaymentStatusReponse;
import uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.model.PaymentInfo;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@Profile("!functional")
public class ProcessorClient {
    private static final Logger logger = LoggerFactory.getLogger(ProcessorClient.class);
    private final AuthTokenGenerator authTokenGenerator;
    private final BulkScanProcessorApiProxy proxy;
    private final RetryTemplate retryTemplate;

    public ProcessorClient(
        AuthTokenGenerator authTokenGenerator,
        BulkScanProcessorApiProxy proxy,
        RetryTemplate retryTemplate
    ) {
        this.authTokenGenerator = authTokenGenerator;
        this.proxy = proxy;
        this.retryTemplate = retryTemplate;
    }

    @Async("AsyncExecutor")
    public CompletableFuture<Boolean> updatePayments(List<PaymentInfo> payments) {
        PaymentRequest request = new PaymentRequest(payments);
        String authToken = authTokenGenerator.generate();
        CompletableFuture<Boolean> completableFuture = new CompletableFuture<>();
        try {
            retryTemplate.execute(context -> {
                logger.info("Started to update payment DCNS {} ", request.payments);
                PaymentStatusReponse paymentStatusReponse = proxy.updateStatus(authToken, request);
                logger.info("Updated payment DCNS {} with reponse {} ", request.payments, paymentStatusReponse.status);
                return completableFuture.complete(true);
            });
        } catch (Exception exception) {
            completableFuture.completeExceptionally(exception);
            logger.error("Exception on payment status update for DCNS {} ", payments, exception);
        }
        return completableFuture;
    }
}
