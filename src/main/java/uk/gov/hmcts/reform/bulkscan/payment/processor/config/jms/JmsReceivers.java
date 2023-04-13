package uk.gov.hmcts.reform.bulkscan.payment.processor.config.jms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.Headers;

import javax.jms.Session;

@Configuration()
@ConditionalOnProperty(name = "jms.enabled", havingValue = "true")
public class JmsReceivers {

    private static final Logger log = LoggerFactory.getLogger(JmsReceivers.class);

    @JmsListener(destination = "payments", containerFactory = "paymentsEventQueueContainerFactory")
    public void receiveMessage(@Headers MessageHeaders headers,
                               Message message, Session session) {
        log.info("- - - - - - - - - - - - - - - - - - - - - - - -");
        log.info("######          Message Details           #####");
        log.info("- - - - - - - - - - - - - - - - - - - - - - - -");
        log.info("headers: " + headers);
        log.info("message: " + message);
        log.info("session: " + session);
        log.info("- - - - - - - - - - - - - - - - - - - - - - - -");
    }
}
