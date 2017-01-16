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

import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class BookService {

	private static final Logger log = LoggerFactory.getLogger(BookService.class);

	/**
	 * Scenario for "should generate tests triggered by a method": client side: must have
	 * a possibility to "trigger" sending of a message to the given messageFrom server
	 * side: will run the method and await upon receiving message on the output
	 * messageFrom
	 * 
	 * Method triggers sending a message to a source
	 */
	public void returnBook(Exchange exchange) {
		BookReturned bookReturned = exchange.getIn().getBody(BookReturned.class);
		log.info("Returning book [$bookReturned]");
		exchange.getOut().setBody(bookReturned);
		exchange.getOut().setHeader("BOOK-NAME", bookReturned.bookName);
	}
}
