/*
 * Copyright 2013-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example;

import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

interface BookService {

	void sendBook(Book book, String replyTo);

	void newBook(Book book);

	Book getBook(int index);

	int noOfBooks();

	List<Book> getBooks();

}

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

	@RabbitListener(
			bindings = @QueueBinding(
					value = @Queue, exchange = @Exchange(value = "input",
							durable = "true", autoDelete = "false", type = "topic"),
					key = "event"))
	// Workaround for https://github.com/spring-projects/spring-amqp/issues/1285
	// public void newBook(Book book, @Headers Map<String, String> headers) {
	public void newBook(Book book, @Header("amqp_replyTo") String replyTo) {
		LOG.info("Received new book with bookname = " + book.getName());
		// LOG.info("Headers = " + headers);
		this.service.sendBook(book, replyTo);
	}

	@RabbitListener(
			bindings = @QueueBinding(
					value = @Queue, exchange = @Exchange(value = "input",
							durable = "true", autoDelete = "false", type = "topic"),
					key = "event2"))
	// Workaround for https://github.com/spring-projects/spring-amqp/issues/1285
	// public void newBook2(Book book, @Headers Map<String, String> headers) {
	public void newBook2(Book book, @Header("amqp_replyTo") String replyTo) {
		LOG.info("newBook2 Received new book with bookname = " + book.getName());
		// LOG.info("newBook2 Headers = " + headers);
		this.service.sendBook(book, replyTo);
	}

}

@Component
class BookServiceImpl implements BookService {

	public static final Logger LOG = LoggerFactory.getLogger(BookServiceImpl.class);

	private List<Book> books;

	private RabbitTemplate rabbitTemplate;

	@Autowired
	BookServiceImpl(RabbitTemplate rabbitTemplate) {
		this.books = new LinkedList<>();
		this.rabbitTemplate = rabbitTemplate;
	}

	@Override
	public void sendBook(Book book, String replyTo) {
		LOG.info("Received new book with bookname = " + book.getName());
		newBook(book);
		this.rabbitTemplate.convertAndSend("", replyTo, book);
	}

	@Override
	public void newBook(Book book) {
		this.books.add(book);
	}

	@Override
	public Book getBook(int index) {
		return this.books.get(index);
	}

	@Override
	public int noOfBooks() {
		return this.books.size();
	}

	@Override
	public List<Book> getBooks() {
		return this.books;
	}

}
