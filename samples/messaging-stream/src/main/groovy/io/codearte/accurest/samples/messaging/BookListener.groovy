package io.codearte.accurest.samples.messaging

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.stream.annotation.EnableBinding
import org.springframework.cloud.stream.annotation.StreamListener
import org.springframework.cloud.stream.messaging.Sink
import org.springframework.cloud.stream.messaging.Source
import org.springframework.messaging.MessageHeaders
import org.springframework.messaging.support.MessageBuilder

import java.util.concurrent.atomic.AtomicBoolean

@CompileStatic
@Slf4j
@EnableBinding([DeleteSink, Sink])
class BookListener {

	@Autowired Source source

	/**
	   Scenario for "should generate tests triggered by a message":
	     client side: if sends a message to input.messageFrom then message will be sent to output.messageFrom
	     server side: will send a message to input, verify the message contents and await upon receiving message on the output messageFrom
	 */
	@StreamListener(Sink.INPUT)
	void returnBook(BookReturned bookReturned) {
		log.info("Returning book [$bookReturned]")
		source.output().send(MessageBuilder.createMessage(bookReturned, new MessageHeaders([
		        'BOOK-NAME': bookReturned.bookName as Object
		])))
	}

	/**
	   Scenario for "should generate tests triggered by a message":
	     client side: if sends a message to input.messageFrom then message will be sent to output.messageFrom
	     server side: will send a message to input, verify the message contents and await upon receiving message on the output messageFrom
	 */
	@StreamListener(DeleteSink.INPUT)
	void bookDeleted(BookDeleted bookDeleted) {
		log.info("Deleting book [$bookDeleted]")
		// ... doing some work
		bookSuccessfulyDeleted.set(true)
	}

	AtomicBoolean bookSuccessfulyDeleted = new AtomicBoolean(false)
}
