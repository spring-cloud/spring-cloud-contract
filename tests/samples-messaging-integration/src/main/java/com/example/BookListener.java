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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

public class BookListener {

	private static final Logger log = LoggerFactory.getLogger(BookListener.class);

	/**
	 * Scenario for "should generate tests triggered by a message": client side: if sends
	 * a message to input.messageFrom then message will be sent to output.messageFrom
	 * server side: will send a message to input, verify the message contents and await
	 * upon receiving message on the output messageFrom.
	 * @param bookReturned - payload
	 * @return message with the returned book
	 */
	public Message<BookReturned> returnBook(BookReturned bookReturned) {
		log.info("Returning book [" + bookReturned + "]");
		return MessageBuilder.withPayload(bookReturned).setHeader("BOOK-NAME", bookReturned.bookName).build();
	}

}
