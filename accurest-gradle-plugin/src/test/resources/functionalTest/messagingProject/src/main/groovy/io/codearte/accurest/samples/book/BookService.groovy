package io.codearte.accurest.samples.book

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.MessageHeaders
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Service

@Service
@CompileStatic
@Slf4j
class BookService {

	@Autowired @Qualifier("outputChannel") MessageChannel outputChannel

	/**
	   Scenario for "should generate tests triggered by a method":
	      client side: must have a possibility to "trigger" sending of a message to the given messageFrom
	      server side: will run the method and await upon receiving message on the output messageFrom

	   Method triggers sending a message to a source
	 */
	void returnBook(BookReturned bookReturned) {
		log.info("Returning book [$bookReturned]")
		outputChannel.send(MessageBuilder.createMessage(bookReturned, new MessageHeaders([
		        'BOOK-NAME': bookReturned.bookName as Object
		])))
	}
}
