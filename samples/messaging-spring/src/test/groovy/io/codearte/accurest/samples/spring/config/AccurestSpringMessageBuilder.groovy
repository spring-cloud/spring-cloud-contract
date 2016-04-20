package io.codearte.accurest.samples.spring.config

import io.codearte.accurest.messaging.AccurestMessage
import io.codearte.accurest.messaging.AccurestMessageBuilder
import org.springframework.jms.core.MessageCreator

import javax.jms.JMSException
import javax.jms.Message
import javax.jms.Session
/**
 * @author Marcin Grzejszczak
 */
public class AccurestSpringMessageBuilder<T extends Serializable> implements AccurestMessageBuilder<T, Message> {

	@Override
	public AccurestMessage<T, Message> create(final T payload, final Map<String, Object> headers) {
		MessageCreator messageCreator = new MessageCreator() {
			@Override
			public Message createMessage(Session session) throws JMSException {
				Message message = session.createObjectMessage(payload);
				headers.entrySet().each { Map.Entry<String, Object> entry ->
					message.setObjectProperty(entry.key, entry.value)
				}
				return message
			}
		};

		return new SpringMessage<>(messageCreator)
	}

	@Override
	public AccurestMessage<T, Message> create(Message message) {
		return new SpringMessage<>(message);
	}
}
