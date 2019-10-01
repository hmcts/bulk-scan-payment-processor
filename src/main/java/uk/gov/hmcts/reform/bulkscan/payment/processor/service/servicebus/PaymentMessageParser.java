package uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.microsoft.azure.servicebus.MessageBody;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.exceptions.InvalidMessageException;
import uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.model.PaymentMessage;

import java.io.IOException;
import java.util.List;

public class PaymentMessageParser {

    private static final ObjectMapper objectMapper;

    static {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS);
    }

    private PaymentMessageParser() {
        // utility class
    }

    public static PaymentMessage parse(MessageBody messageBody) {
        try {
            return objectMapper.readValue(getBinaryData(messageBody), PaymentMessage.class);
        } catch (IOException exc) {
            throw new InvalidMessageException(exc);
        }
    }

    private static byte[] getBinaryData(MessageBody messageBody) {
        List<byte[]> binaryData = messageBody.getBinaryData();

        return CollectionUtils.isEmpty(binaryData) ? null : binaryData.get(0);
    }

}
