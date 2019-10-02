package uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.servicebus.MessageBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.exceptions.InvalidMessageException;
import uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.model.PaymentMessage;

import java.io.IOException;
import java.util.List;

@Service
public class PaymentMessageParser {

    @Autowired
    private final ObjectMapper objectMapper;

    private static final Throwable ERROR_CAUSE = new RuntimeException("Message Binary data is null");

    public PaymentMessageParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public PaymentMessage parse(MessageBody messageBody) {
        try {
            return objectMapper.readValue(getBinaryData(messageBody), PaymentMessage.class);
        } catch (IOException exc) {
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
