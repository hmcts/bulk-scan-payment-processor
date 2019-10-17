package uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.servicebus.MessageBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.exceptions.InvalidMessageException;
import uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.model.PaymentMessage;

import java.io.IOException;
import java.util.List;

@Service
public class PaymentMessageParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentMessageParser.class);

    private final ObjectMapper objectMapper;

    private static final String ERROR_CAUSE = "Message Binary data is null";

    public PaymentMessageParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public <T extends PaymentMessage> T parse(MessageBody messageBody, Class<T> clazz) {
        try {
            T payment = objectMapper.readValue(getBinaryData(messageBody), clazz);
            LOGGER.info("Parsed Payment message {}", payment);
            return payment;
        } catch (IOException exc) {
            LOGGER.error("Payment queue message, parse error ", exc);
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
}
