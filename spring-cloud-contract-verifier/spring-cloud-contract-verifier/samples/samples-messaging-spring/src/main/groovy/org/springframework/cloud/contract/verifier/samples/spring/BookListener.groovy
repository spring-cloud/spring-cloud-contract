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

package org.springframework.cloud.contract.verifier.samples.spring

import com.fasterxml.jackson.databind.ObjectMapper
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jms.annotation.JmsListener
import org.springframework.jms.core.JmsTemplate
import org.springframework.jms.core.MessageCreator
import org.springframework.stereotype.Service

import javax.jms.JMSException
import javax.jms.Message
import javax.jms.Session
import java.util.concurrent.atomic.AtomicBoolean

@CompileStatic
@Slf4j
@Service
class BookListener {

	@Autowired JmsTemplate jmsTemplate
	ObjectMapper objectMapper = new ObjectMapper()

	/**
	 Scenario for "should generate tests triggered by a message":
	 client side: if sends a message to input.messageFrom then message will be sent to output.messageFrom
	 server side: will send a message to input, verify the message contents and await upon receiving message on the output messageFrom
	 */
	@JmsListener(destination = "input")
	void returnBook(String messageAsString) {
		BookReturned bookReturned = objectMapper.readerFor(BookReturned).readValue(messageAsString) as BookReturned
		log.info("Returning book [$bookReturned]")
		MessageCreator messageCreator = new MessageCreator() {
			@Override
			public Message createMessage(Session session) throws JMSException {
				Message message = session.createObjectMessage(bookReturned);
				message.setStringProperty('BOOK-NAME', bookReturned.bookName)
				return message
			}
		};
		jmsTemplate.send('output', messageCreator)
	}

	/**
	   Scenario for "should generate tests triggered by a message":
	     client side: if sends a message to input.messageFrom then message will be sent to output.messageFrom
	     server side: will send a message to input, verify the message contents and await upon receiving message on the output messageFrom
	 */
	@JmsListener(destination = "delete")
	void bookDeleted(String bookDeletedAsString) {
		BookDeleted bookDeleted = objectMapper.readerFor(BookDeleted).readValue(bookDeletedAsString) as BookDeleted
		log.info("Deleting book [$bookDeleted]")
		bookSuccessfulyDeleted.set(true)
	}

	AtomicBoolean bookSuccessfulyDeleted = new AtomicBoolean(false)
}
