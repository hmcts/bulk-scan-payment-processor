package uk.gov.hmcts.reform.bulkscan.payment.processor.config.jms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.ErrorHandler;

@Service
public class JmsErrorHandler implements ErrorHandler {

    private static final Logger log = LoggerFactory.getLogger(JmsReceivers.class);

    @Override
    public void handleError(Throwable t) {
        log.info(t.getMessage());
    }
}

