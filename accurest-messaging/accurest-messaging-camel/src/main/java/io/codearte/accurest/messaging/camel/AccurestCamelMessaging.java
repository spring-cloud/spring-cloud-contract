package io.codearte.accurest.messaging.camel;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.camel.CamelContext;
import org.apache.camel.ConsumerTemplate;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.impl.DefaultExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.codearte.accurest.messaging.AccurestMessage;
import io.codearte.accurest.messaging.AccurestMessageBuilder;
import io.codearte.accurest.messaging.AccurestMessaging;

/**
 * @author Marcin Grzejszczak
 */
@Component
public class AccurestCamelMessaging<T> implements AccurestMessaging<T, Message> {

	private static final Logger log = LoggerFactory.getLogger(AccurestCamelMessaging.class);

	private final CamelContext context;
	private final AccurestMessageBuilder builder;

	@Autowired
	@SuppressWarnings("unchecked")
	public AccurestCamelMessaging(CamelContext context, AccurestMessageBuilder accurestMessageBuilder) {
		this.context = context;
		this.builder = accurestMessageBuilder;
	}

	@Override
	public void send(AccurestMessage<T, Message> message, String destination) {
		try {
			ProducerTemplate producerTemplate = context.createProducerTemplate();
			Exchange exchange = new DefaultExchange(context);
			exchange.setIn(message.convert());
			producerTemplate.send(destination, exchange);
		} catch (Exception e) {
			log.error("Exception occurred while trying to send a message [" + message + "] " +
					"to a channel with name [" + destination + "]", e);
			throw e;
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public void send(T payload, Map<String, Object> headers, String destination) {
		send(builder.create(payload, headers), destination);
	}

	@Override
	@SuppressWarnings("unchecked")
	public AccurestMessage<T, Message> receiveMessage(String destination, long timeout, TimeUnit timeUnit) {
		try {
			ConsumerTemplate consumerTemplate = context.createConsumerTemplate();
			Exchange exchange = consumerTemplate.receive(destination, timeUnit.toMillis(timeout));
			return builder.create(exchange.getIn());
		} catch (Exception e) {
			log.error("Exception occurred while trying to read a message from " +
					" a channel with name [" + destination + "]", e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public AccurestMessage<T, Message> receiveMessage(String destination) {
		return receiveMessage(destination, 5, TimeUnit.SECONDS);
	}

	@Override
	@SuppressWarnings("unchecked")
	public AccurestMessage<T, Message> create(T t, Map<String, Object> headers) {
		return builder.create(t, headers);
	}

	@Override
	@SuppressWarnings("unchecked")
	public AccurestMessage<T, Message> create(Message message) {
		return builder.create(message);
	}
}
