package io.codearte.accurest.samples.book;

import org.assertj.core.api.Assertions;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationContextLoader;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Marcin Grzejszczak
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {IntegrationMessagingApplication.class}, loader = SpringApplicationContextLoader.class)
public abstract class MessagingBaseTest {

	// BASE CLASS WOULD HAVE THIS:

	@Autowired BookService bookService;
	@Autowired BookListener bookListener;

	public void bookReturnedTriggered() {
		bookService.returnBook(new BookReturned("foo"));
	}

	public void bookWasDeleted() {
		Assertions.assertThat(bookListener.getBookSuccessfulyDeleted().get()).isTrue();
	}
}
