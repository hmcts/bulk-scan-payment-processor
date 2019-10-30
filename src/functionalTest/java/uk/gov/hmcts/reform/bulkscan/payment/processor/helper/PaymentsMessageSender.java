package uk.gov.hmcts.reform.bulkscan.payment.processor.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.servicebus.IMessage;
import com.microsoft.azure.servicebus.Message;
import com.microsoft.azure.servicebus.QueueClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.bulkscan.payment.processor.FunctionalQueueConfig;
import uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.model.CreatePaymentMessage;

import java.time.Instant;
import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON;

@Service
@Import(FunctionalQueueConfig.class)
public class PaymentsMessageSender {

    private static final Logger LOG = LoggerFactory.getLogger(PaymentsMessageSender.class);

    private static final String CREATE = "CREATE";

    @Autowired
    @Qualifier("payments")
    private QueueClient queueClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public void send(CreatePaymentMessage cmd) {
        try {
            final String messageContent = objectMapper.writeValueAsString(cmd);

            IMessage message = new Message(
                "test_msg_" + UUID.randomUUID().toString(),
                messageContent,
                APPLICATION_JSON.toString()
            );
            message.setLabel(CREATE);

            long res = queueClient.scheduleMessage(message, Instant.now());

            LOG.info(
                "Sent message to payments queue. Result: {}, ID: {}, Label: {}, Content: {}",
                res,
                message.getMessageId(),
                message.getLabel(),
                messageContent
            );
        } catch (Exception ex) {
            throw new RuntimeException(
                "An error occurred when trying to publish message to payments queue.",
                ex
            );
        }
    }
}
