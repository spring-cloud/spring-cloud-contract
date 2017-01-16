/*
 *  Copyright 2013-2017 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.example;

import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.messaging.support.MessageBuilder;

@EnableBinding({ DeleteSink.class, Sink.class })
public class BookListener {

	private static final Logger log = LoggerFactory.getLogger(BookListener.class);

	@Autowired
	private Source source;

	/**
	 * Scenario for "should generate tests triggered by a message": client side: if sends
	 * a message to input.messageFrom then message will be sent to output.messageFrom
	 * server side: will send a message to input, verify the message contents and await
	 * upon receiving message on the output messageFrom
	 */
	@StreamListener(Sink.INPUT)
	public void returnBook(BookReturned bookReturned) {
		log.info("Returning book " + bookReturned);
		this.source.output().send(MessageBuilder.withPayload(bookReturned)
				.setHeader("BOOK-NAME", bookReturned.bookName).build());
	}

	/**
	 * Scenario for "should generate tests triggered by a message": client side: if sends
	 * a message to input.messageFrom then message will be sent to output.messageFrom
	 * server side: will send a message to input, verify the message contents and await
	 * upon receiving message on the output messageFrom
	 */
	@StreamListener(DeleteSink.INPUT)
	public void bookDeleted(BookDeleted bookDeleted) {
		log.info("Deleting book " + bookDeleted);
		// ... doing some work
		this.bookSuccessfulyDeleted.set(true);
	}

	public AtomicBoolean bookSuccessfulyDeleted = new AtomicBoolean(false);
}
