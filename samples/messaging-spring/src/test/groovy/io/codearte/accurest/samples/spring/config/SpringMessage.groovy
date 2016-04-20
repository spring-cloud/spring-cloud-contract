package io.codearte.accurest.samples.spring.config

import io.codearte.accurest.messaging.AccurestMessage
import org.springframework.jms.core.MessageCreator

import javax.jms.Message
import javax.jms.ObjectMessage
/**
 * @author Marcin Grzejszczak
 */
public class SpringMessage<T> implements AccurestMessage<T, Message> {

	private final ObjectMessage messageDelegate;
	final MessageCreator messageCreator;

	public SpringMessage(Message messageDelegate) {
		this.messageDelegate = (ObjectMessage) messageDelegate;
		this.messageCreator = null
	}

	public SpringMessage(MessageCreator messageCreator) {
		this.messageDelegate = null;
		this.messageCreator = messageCreator
	}

	@Override
	public T getPayload() {
		return messageDelegate.getObject();
	}

	@Override
	public Map<String, Object> getHeaders() {
		Map<String, Object> headers = [:]
		def enumeration = messageDelegate.propertyNames
		while(enumeration.hasMoreElements()) {
			String propertyName = enumeration.nextElement()
			headers << [(propertyName) : messageDelegate.getObjectProperty(propertyName)]
		}
		return headers
	}

	@Override
	Object getHeader(String key) {
		return getHeaders().get(key)
	}

	@Override
	public Message convert() {
		return messageDelegate;
	}

}
