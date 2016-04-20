package io.codearte.accurest.samples.camel

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.apache.camel.Exchange
import org.springframework.stereotype.Service

@Service
@CompileStatic
@Slf4j
class BookService {

	/**
	   Scenario for "should generate tests triggered by a method":
	      client side: must have a possibility to "trigger" sending of a message to the given messageFrom
	      server side: will run the method and await upon receiving message on the output messageFrom

	   Method triggers sending a message to a source
	 */
	void returnBook(Exchange exchange) {
		BookReturned bookReturned = exchange.in.getBody(BookReturned)
		log.info("Returning book [$bookReturned]")
		exchange.out.with {
			body = bookReturned
			setHeader('BOOK-NAME', bookReturned.bookName)
		}
	}
}
