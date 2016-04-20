package io.codearte.accurest.samples.spring

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jms.core.JmsTemplate
import org.springframework.jms.core.MessageCreator
import org.springframework.stereotype.Service

import javax.jms.JMSException
import javax.jms.Message
import javax.jms.Session

@Service
@CompileStatic
@Slf4j
class BookService {

	@Autowired JmsTemplate jmsTemplate

	/**
	   Scenario for "should generate tests triggered by a method":
	      client side: must have a possibility to "trigger" sending of a message to the given messageFrom
	      server side: will run the method and await upon receiving message on the output messageFrom

	   Method triggers sending a message to a source
	 */
	void returnBook(BookReturned bookReturned ) {
		log.info("Returning book [$bookReturned]")
		MessageCreator messageCreator = new MessageCreator() {
			@Override
			public Message createMessage(Session session) throws JMSException {
				Message message = session.createObjectMessage(bookReturned);
				message.setStringProperty('BOOK-NAME', bookReturned.bookName)
				return message
			}
		};
		jmsTemplate.send('output', messageCreator)
	}
}
