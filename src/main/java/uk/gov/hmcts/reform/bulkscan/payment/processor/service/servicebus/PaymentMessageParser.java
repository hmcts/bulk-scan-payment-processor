package uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus;

import com.azure.core.util.BinaryData;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.exceptions.InvalidMessageException;
import uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.model.CreatePaymentMessage;
import uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.model.UpdatePaymentMessage;

import java.io.IOException;

/**
 * Parses the payment message from the queue.
 */
@Service
@Profile("!functional")
public class PaymentMessageParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentMessageParser.class);

    private final ObjectMapper objectMapper;

    /**
     * Constructor for the PaymentMessageParser.
     * @param objectMapper The object mapper
     */
    public PaymentMessageParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Parse the payment message.
     * @param messageBody The message body
     * @return The parsed payment message
     */
    public CreatePaymentMessage parse(BinaryData messageBody) {
        try {
            CreatePaymentMessage payment =
                objectMapper.readValue(messageBody.toStream(), CreatePaymentMessage.class);
            logMessageParsed(payment);
            return payment;
        } catch (IOException exc) {
            LOGGER.error("Payment queue message, parse error", exc);
            throw new InvalidMessageException(exc);
        }
    }

    /**
     * Parse the update payment message.
     * @param messageBody The message body
     * @return The parsed payment message
     */
    public UpdatePaymentMessage parseUpdateMessage(BinaryData messageBody) {
        try {
            UpdatePaymentMessage payment = objectMapper.readValue(
                messageBody.toStream(),
                UpdatePaymentMessage.class
            );
            logMessageParsed(payment);
            return payment;
        } catch (IOException exc) {
            LOGGER.error("Payment queue message, parse error", exc);
            throw new InvalidMessageException(exc);
        }
    }

    /**
     * Log the parsed message.
     * @param payment The payment message
     */
    private void logMessageParsed(CreatePaymentMessage payment) {
        LOGGER.info(
            "Parsed Payment message, Envelope ID: {}, CCD Case Number: {}, Is Exception Record: {}, Jurisdiction: {}, "
                + "PO Box: {}, Service: {}, Document Control Numbers: {}",
            payment.envelopeId,
            payment.ccdReference,
            payment.isExceptionRecord,
            payment.jurisdiction,
            payment.poBox,
            payment.service,
            payment.payments
        );
    }

    /**
     * Log the parsed message.
     * @param payment The payment message
     */
    private void logMessageParsed(UpdatePaymentMessage payment) {
        LOGGER.info(
            "Parsed Payment message, Envelope ID: {}, Jurisdiction: {}, "
                + "Exception Record Ref: {}, newCaseRef: {}",
            payment.envelopeId,
            payment.jurisdiction,
            payment.exceptionRecordRef,
            payment.newCaseRef
        );
    }
}
