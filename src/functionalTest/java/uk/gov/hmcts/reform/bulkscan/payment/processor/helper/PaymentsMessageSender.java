package uk.gov.hmcts.reform.bulkscan.payment.processor.helper;

import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.bulkscan.payment.processor.FunctionalQueueConfig;
import uk.gov.hmcts.reform.bulkscan.payment.processor.model.CreatePaymentsCommand;

import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON;

@Service
@Import(FunctionalQueueConfig.class)
@Profile("functional")
public class PaymentsMessageSender {

    private static final Logger log = LoggerFactory.getLogger(PaymentsMessageSender.class);

    private static final String CREATE_PAYMENT_LABEL = "CREATE";

    @Autowired
    @Qualifier("payments")
    private ServiceBusSenderClient serviceBusSenderClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public void send(CreatePaymentsCommand cmd) {
        try {
            final String messageContent = objectMapper.writeValueAsString(cmd);

            ServiceBusMessage message = new ServiceBusMessage(
                messageContent
            );

            message.setMessageId("test_msg_" + UUID.randomUUID());
            message.setContentType(APPLICATION_JSON.toString());
            message.setSubject(CREATE_PAYMENT_LABEL);

            serviceBusSenderClient.sendMessage(message);

            log.info(
                "Sent message to payments queue. ID: {}, Label: {}, Content: {}",
                message.getMessageId(),
                message.getSubject(),
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
