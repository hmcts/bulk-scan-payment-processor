package uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus;

import com.microsoft.azure.servicebus.MessageBody;
import uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.exceptions.IllegalPaymentOperationException;
import uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.handler.PaymentMessageHandler;
import uk.gov.hmcts.reform.bulkscan.payment.processor.service.servicebus.model.PaymentMessage;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public enum PaymentOperation {

    CREATE(PaymentMessageHandler::handlePaymentMessage, PaymentMessageParser::parse),
    UPDATE(PaymentMessageHandler::updatePaymentCaseReference, PaymentMessageParser::parseUpdateMessage);

    public final BiConsumer<PaymentMessageHandler, PaymentMessage> handler;
    public final BiFunction<PaymentMessageParser, MessageBody, PaymentMessage> parser;

    PaymentOperation(
        BiConsumer<PaymentMessageHandler, PaymentMessage> handler,
        BiFunction<PaymentMessageParser, MessageBody, PaymentMessage> parser) {
        this.handler = handler;
        this.parser = parser;
    }

    public static PaymentOperation valueFromStr(String operation) {
        try {
            return PaymentOperation.valueOf(operation);
        } catch (IllegalArgumentException ex) {
            throw new IllegalPaymentOperationException("Operation:" + operation + " is not a valid Payment operation");
        }
    }

}
