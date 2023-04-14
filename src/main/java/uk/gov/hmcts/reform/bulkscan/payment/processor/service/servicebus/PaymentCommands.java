package uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus;

import com.azure.core.util.BinaryData;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.bulkscan.payment.processor.client.processor.ProcessorClient;
import uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.exceptions.InvalidMessageException;
import uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.handler.MessageProcessingResult;
import uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.handler.PaymentMessageHandler;
import uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.model.CreatePaymentMessage;
import uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.model.UpdatePaymentMessage;

import static uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.handler.MessageProcessingResultType.POTENTIALLY_RECOVERABLE_FAILURE;
import static uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.handler.MessageProcessingResultType.SUCCESS;
import static uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.handler.MessageProcessingResultType.UNRECOVERABLE_FAILURE;

@Service
@Profile("!functional")
public class PaymentCommands {

    private static final Logger log = LoggerFactory.getLogger(PaymentCommands.class);

    private final PaymentMessageHandler paymentMessageHandler;
    private final PaymentMessageParser paymentMessageParser;
    private final ProcessorClient processorClient;

    public PaymentCommands(
        PaymentMessageHandler paymentMessageHandler,
        PaymentMessageParser paymentMessageParser,
        ProcessorClient processorClient
    ) {
        this.paymentMessageHandler = paymentMessageHandler;
        this.paymentMessageParser = paymentMessageParser;
        this.processorClient = processorClient;
    }

    MessageProcessingResult processCreateCommand(String messageId, BinaryData body) {
        log.info("Started processing payment message with ID {}", messageId);

        CreatePaymentMessage payment = null;

        try {
            payment = paymentMessageParser.parse(body);
            paymentMessageHandler.handlePaymentMessage(payment, messageId);
            processorClient.updatePayments(payment.payments);
            log.info(
                "Processed payment message with ID {}. Envelope ID: {}",
                messageId,
                payment.envelopeId
            );

            return new MessageProcessingResult(SUCCESS);
        } catch (InvalidMessageException ex) {
            log.error("Rejected payment message with ID {}, because it's invalid", messageId, ex);
            return new MessageProcessingResult(UNRECOVERABLE_FAILURE, ex);
        } catch (Exception ex) {
            logMessageProcessingError(messageId, payment, ex);
            return new MessageProcessingResult(POTENTIALLY_RECOVERABLE_FAILURE);
        }
    }

    MessageProcessingResult processUpdateCommand(String messageId, BinaryData messageBody) {
        log.info("Started processing update payment message with ID {}", messageId);

        UpdatePaymentMessage payment = null;

        try {
            payment = paymentMessageParser.parseUpdateMessage(messageBody);
            paymentMessageHandler.updatePaymentCaseReference(payment);
            log.info(
                "Processed update payment message with ID {}. Envelope ID: {}",
                messageId,
                payment.envelopeId
            );
            return new MessageProcessingResult(SUCCESS);
        } catch (InvalidMessageException ex) {
            log.error(
                "Rejected update payment message with ID {}, because it's invalid",
                messageId,
                ex
            );
            return new MessageProcessingResult(UNRECOVERABLE_FAILURE, ex);
        } catch (Exception ex) {
            logUpdateMessageProcessingError(messageId, payment, ex);
            return new MessageProcessingResult(POTENTIALLY_RECOVERABLE_FAILURE);
        }
    }

    private void logUpdateMessageProcessingError(
        String messageId,
        UpdatePaymentMessage paymentMessage,
        Exception exception
    ) {
        String baseMessage = String.format(
            "Failed to process update payment message with ID %s",
            messageId
        );
        String fullMessage = paymentMessage != null
            ? String.format(
            "%s. New Case Number: %s, Exception Record Ref: %s, Jurisdiction: %s",
            baseMessage,
            paymentMessage.newCaseRef,
            paymentMessage.exceptionRecordRef,
            paymentMessage.jurisdiction
        )
            : baseMessage;
        String fullMessageWithClientResponse = exception instanceof FeignException feignException
            ? String.format("%s. Client response: %s", fullMessage, ((FeignException) exception).contentUTF8())
            : fullMessage;

        log.error(fullMessageWithClientResponse, exception);
    }

    private void logMessageProcessingError(
        String messageId,
        CreatePaymentMessage paymentMessage,
        Exception exception
    ) {
        String baseMessage = String.format("Failed to process payment message with ID %s", messageId);
        String fullMessage = paymentMessage != null
            ? String.format(
            "%s. CCD Case Number: %s, Jurisdiction: %s",
            baseMessage,
            paymentMessage.ccdReference,
            paymentMessage.jurisdiction
        )
            : baseMessage;
        String fullMessageWithClientResponse = exception instanceof FeignException feignException
            ? String.format("%s. Client response: %s", fullMessage, ((FeignException) exception).contentUTF8())
            : fullMessage;

        log.error(fullMessageWithClientResponse, exception);
    }
}
