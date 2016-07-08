/*
 *  Copyright 2013-2016 the original author or authors.
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

package org.springframework.cloud.contract.verifier.samples.messaging

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.messaging.Message
import org.springframework.messaging.MessageHeaders
import org.springframework.messaging.support.MessageBuilder

import java.util.concurrent.atomic.AtomicBoolean

@CompileStatic
@Slf4j
class BookListener {

	/**
	   Scenario for "should generate tests triggered by a message":
	     client side: if sends a message to input.messageFrom then message will be sent to output.messageFrom
	     server side: will send a message to input, verify the message contents and await upon receiving message on the output messageFrom
	 */
	Message returnBook(BookReturned bookReturned) {
		log.info("Returning book [$bookReturned]")
		return MessageBuilder.createMessage(bookReturned, new MessageHeaders([
		        'BOOK-NAME': bookReturned.bookName as Object
		]))
	}

	/**
	   Scenario for "should generate tests triggered by a message":
	     client side: if sends a message to input.messageFrom then message will be sent to output.messageFrom
	     server side: will send a message to input, verify the message contents and await upon receiving message on the output messageFrom
	 */
	void bookDeleted(BookDeleted bookDeleted) {
		log.info("Deleting book [$bookDeleted]")
		bookSuccessfulyDeleted.set(true)
	}

	AtomicBoolean bookSuccessfulyDeleted = new AtomicBoolean(false)
}
