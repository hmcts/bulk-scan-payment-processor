package uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.servicebus.MessageBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.exceptions.InvalidMessageException;
import uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.model.CreatePaymentMessage;
import uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.model.UpdatePaymentMessage;

import java.io.IOException;
import java.util.List;

@Service
@Profile("!functional")
public class PaymentMessageParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentMessageParser.class);

    private final ObjectMapper objectMapper;

    private static final String ERROR_CAUSE = "Message Binary data is null";

    public PaymentMessageParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public CreatePaymentMessage parse(MessageBody messageBody) {
        try {
            CreatePaymentMessage payment =
                objectMapper.readValue(getBinaryData(messageBody), CreatePaymentMessage.class);
            logMessageParsed(payment);
            return payment;
        } catch (IOException exc) {
            LOGGER.error("Payment queue message, parse error", exc);
            throw new InvalidMessageException(exc);
        }
    }

    public UpdatePaymentMessage parseUpdateMessage(MessageBody messageBody) {
        try {
            UpdatePaymentMessage payment = objectMapper.readValue(
                getBinaryData(messageBody),
                UpdatePaymentMessage.class
            );
            logMessageParsed(payment);
            return payment;
        } catch (IOException exc) {
            LOGGER.error("Payment queue message, parse error", exc);
            throw new InvalidMessageException(exc);
        }
    }

    private static byte[] getBinaryData(MessageBody messageBody) {
        List<byte[]> binaryData = messageBody.getBinaryData();
        if (binaryData == null) {
            throw new InvalidMessageException(ERROR_CAUSE);
        }

        return CollectionUtils.isEmpty(binaryData) ? null : binaryData.get(0);
    }

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
