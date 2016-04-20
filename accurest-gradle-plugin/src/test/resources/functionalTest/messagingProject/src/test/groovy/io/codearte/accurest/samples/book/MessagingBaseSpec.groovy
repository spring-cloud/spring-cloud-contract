package io.codearte.accurest.samples.book

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.SpringApplicationContextLoader
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification
/**
 * @author Marcin Grzejszczak
 */
@ContextConfiguration(classes = [IntegrationMessagingApplication], loader = SpringApplicationContextLoader)
abstract class MessagingBaseSpec extends Specification {

	// BASE CLASS WOULD HAVE THIS:

	@Autowired BookService bookService
	@Autowired BookListener bookListener

	void bookReturnedTriggered() {
		bookService.returnBook(new BookReturned("foo"))
	}

	void bookWasDeleted() {
		assert bookListener.bookSuccessfulyDeleted.get()
	}
}
