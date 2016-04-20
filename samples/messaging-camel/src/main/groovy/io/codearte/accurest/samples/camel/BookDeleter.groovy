package io.codearte.accurest.samples.camel

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.apache.camel.Exchange
import org.springframework.stereotype.Component

import java.util.concurrent.atomic.AtomicBoolean

@CompileStatic
@Slf4j
@Component
class BookDeleter {

	/**
	   Scenario for "should generate tests triggered by a message":
	     client side: if sends a message to input.messageFrom then message will be sent to output.messageFrom
	     server side: will send a message to input, verify the message contents and await upon receiving message on the output messageFrom
	 */
	void bookDeleted(Exchange exchange) {
		BookDeleted bookDeleted = exchange.in.getBody(BookDeleted)
		log.info("Deleting book [$bookDeleted]")
		bookSuccessfulyDeleted.set(true)
		log.info("Book successfuly deleted [$bookSuccessfulyDeleted]")
	}

	AtomicBoolean bookSuccessfulyDeleted = new AtomicBoolean(false)
}
