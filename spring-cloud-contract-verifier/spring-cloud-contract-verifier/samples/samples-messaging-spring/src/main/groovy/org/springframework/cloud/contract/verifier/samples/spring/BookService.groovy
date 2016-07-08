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

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jms.core.JmsTemplate
import org.springframework.jms.core.MessageCreator
import org.springframework.stereotype.Service

import javax.jms.JMSException
import javax.jms.Message
import javax.jms.Session

@Service
@CompileStatic
@Slf4j
class BookService {

	@Autowired JmsTemplate jmsTemplate

	/**
	   Scenario for "should generate tests triggered by a method":
	      client side: must have a possibility to "trigger" sending of a message to the given messageFrom
	      server side: will run the method and await upon receiving message on the output messageFrom

	   Method triggers sending a message to a source
	 */
	void returnBook(BookReturned bookReturned ) {
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
}
