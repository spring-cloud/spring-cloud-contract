package com.example;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.stereotype.Component;

@Component
public class RabbitManager {

	public static final Logger LOG = LoggerFactory.getLogger(RabbitManager.class);
    
    
	private BookService service;
    private RabbitTemplate rabbitTemplate;

    @Autowired
	public RabbitManager(BookService service, RabbitTemplate rabbitTemplate) {
		this.service = service;
		this.rabbitTemplate = rabbitTemplate;
	}

	@RabbitListener(bindings = @QueueBinding(
			value = @Queue(),
			exchange = @Exchange(
					value = "input",
					durable="true",
					autoDelete="false",
					type="topic"),
			key = "event"
	))
	public void newBook(Book book, @Headers Map<String, String> headers) {
		LOG.info("Received new book with bookname = " + book.getName());
		LOG.info("Headers = " + headers);
		service.sendBook(book, headers.get("amqp_replyTo"));
	}

    @RabbitListener(bindings = @QueueBinding(
	 		value = @Queue(),
	 		exchange = @Exchange(
	 				value = "input",
	 				durable="true",
	 				autoDelete="false",
	 				type="topic"),
	 		key = "event2"
	 ))
	 public void newBook2(Book book, @Headers Map<String, String> headers) {
	 	LOG.info("newBook2 Received new book with bookname = " + book.getName());
	 	LOG.info("newBook2 Headers = " + headers);
	 	service.sendBook(book, headers.get("amqp_replyTo"));
	 }
}

interface BookService {
	void sendBook(Book book, String replyTo);

	void newBook(Book book);

	Book getBook(int index);

	int noOfBooks();

	List<Book> getBooks();

}

@Component
class BookServiceImpl implements BookService {
	public static final Logger LOG = LoggerFactory.getLogger(BookServiceImpl.class);

	private List<Book> books;
	private RabbitTemplate rabbitTemplate;

	@Autowired
	public BookServiceImpl(RabbitTemplate rabbitTemplate) {
		books = new LinkedList<>();
		this.rabbitTemplate = rabbitTemplate;
	}

	@Override
	public void sendBook(Book book, String replyTo) {
		LOG.info("Received new book with bookname = " + book.getName());
		newBook(book);
		rabbitTemplate.convertAndSend("", replyTo, book);
	}

	@Override
	public void newBook(Book book) {
		books.add(book);
	}

	@Override
	public Book getBook(int index) {
		return books.get(index);
	}

	@Override
	public int noOfBooks() {
		return books.size();
	}

	@Override
	public List<Book> getBooks() {
		return books;
	}
}
