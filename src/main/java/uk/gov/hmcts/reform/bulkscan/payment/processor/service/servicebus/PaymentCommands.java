package uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus;

import com.azure.core.util.BinaryData;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.bulkscan.payment.processor.client.processor.ProcessorClient;
import uk.gov.hmcts.reform.bulkscan.payment.processor.service.PaymentHubHandlerService;
import uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.exceptions.InvalidMessageException;
import uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.handler.MessageProcessingResult;
import uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.model.CreatePaymentMessage;
import uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.model.UpdatePaymentMessage;

import static uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.handler.MessageProcessingResultType.POTENTIALLY_RECOVERABLE_FAILURE;
import static uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.handler.MessageProcessingResultType.SUCCESS;
import static uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.handler.MessageProcessingResultType.UNRECOVERABLE_FAILURE;

/**
 * Processes payment messages.
 */
@Service
@Profile("!functional")
public class PaymentCommands {

    private static final Logger log = LoggerFactory.getLogger(PaymentCommands.class);

    private final PaymentHubHandlerService paymentHubHandlerService;
    private final PaymentMessageParser paymentMessageParser;
    private final ProcessorClient processorClient;

    /**
     * Constructor for the PaymentCommands.
     * @param paymentHubHandlerService The payment message handler
     * @param paymentMessageParser The payment message parser
     * @param processorClient The processor client
     */
    public PaymentCommands(
        PaymentHubHandlerService paymentHubHandlerService,
        PaymentMessageParser paymentMessageParser,
        ProcessorClient processorClient
    ) {
        this.paymentHubHandlerService = paymentHubHandlerService;
        this.paymentMessageParser = paymentMessageParser;
        this.processorClient = processorClient;
    }

    /**
     * Process the create command.
     * @param messageId The message ID
     * @param body The message body
     * @return The message processing result
     */
    MessageProcessingResult processCreateCommand(String messageId, BinaryData body) {
        log.info("Started processing payment message with ID {}", messageId);

        CreatePaymentMessage payment = null;

        try {
            payment = paymentMessageParser.parse(body);
            paymentHubHandlerService.handlePaymentMessage(payment, messageId);
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

    /**
     * Process the update command.
     * @param messageId The message ID
     * @param messageBody The message body
     * @return The message processing result
     */
    MessageProcessingResult processUpdateCommand(String messageId, BinaryData messageBody) {
        log.info("Started processing update payment message with ID {}", messageId);

        UpdatePaymentMessage payment = null;

        try {
            payment = paymentMessageParser.parseUpdateMessage(messageBody);
            paymentHubHandlerService.updatePaymentCaseReference(payment);
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

    /**
     * Log the update message processing error.
     * @param messageId The message ID
     * @param paymentMessage The payment message
     * @param exception The exception
     */
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

    /**
     * Log processing error.
     * @param messageId The message ID
     * @param paymentMessage The payment message
     * @param exception The exception
     */
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

    /**
     * Log finalise error.
     * @param messageId The message ID
     * @param processingResultType The processing result type
     * @param ex The exception
     */
    public void logMessageFinaliseError(
        String messageId,
        Object processingResultType,
        Exception ex
    ) {
        log.error(
            "Failed to process payment message with ID {}. Processing result: {}",
            messageId,
            processingResultType,
            ex
        );
    }
}
