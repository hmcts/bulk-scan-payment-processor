package uk.gov.hmcts.reform.bulkscan.payment.processor.config.jms;

import com.azure.core.util.BinaryData;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;
import com.azure.messaging.servicebus.ServiceBusReceiverAsyncClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.reflect.TypeToken;
import com.microsoft.applicationinsights.core.dependencies.google.gson.ExclusionStrategy;
import com.microsoft.applicationinsights.core.dependencies.google.gson.FieldAttributes;
import com.microsoft.applicationinsights.core.dependencies.google.gson.Gson;
import com.microsoft.applicationinsights.core.dependencies.google.gson.GsonBuilder;
import com.microsoft.applicationinsights.core.dependencies.google.gson.JsonDeserializationContext;
import com.microsoft.applicationinsights.core.dependencies.google.gson.JsonDeserializer;
import com.microsoft.applicationinsights.core.dependencies.google.gson.JsonElement;
import com.microsoft.applicationinsights.core.dependencies.google.gson.JsonObject;
import com.microsoft.applicationinsights.core.dependencies.google.gson.JsonParseException;
import com.microsoft.applicationinsights.core.dependencies.google.gson.TypeAdapter;
import com.microsoft.applicationinsights.core.dependencies.google.gson.stream.JsonReader;
import com.microsoft.applicationinsights.core.dependencies.google.gson.stream.JsonWriter;
import org.apache.activemq.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.server.Session;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.JmsListener;
import uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.PaymentMessageProcessor;
import uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.model.CreatePaymentMessage;
import uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.model.PaymentInfo;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;

@Configuration()
@ConditionalOnProperty(name = "jms.enabled", havingValue = "true")
public class JmsReceivers {

    private static final Logger log = LoggerFactory.getLogger(JmsReceivers.class);

    private final PaymentMessageProcessor paymentMessageProcessor;

    public JmsReceivers(
        PaymentMessageProcessor paymentMessageProcessor
    ) {
        this.paymentMessageProcessor = paymentMessageProcessor;
    }

    @JmsListener(destination = "payments", containerFactory = "paymentsEventQueueContainerFactory")
    public void receiveMessage(String message) {
        log.info("Received Person {} on Service Bus", message);

//        CreatePaymentMessage paymentInfo = new Gson().fromJson(message, CreatePaymentMessage.class);

        log.info("Finished {}", message);
    }
}
