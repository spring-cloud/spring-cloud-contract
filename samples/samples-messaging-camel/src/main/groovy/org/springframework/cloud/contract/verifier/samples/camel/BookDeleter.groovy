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

package org.springframework.cloud.contract.verifier.samples.camel

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.apache.camel.Exchange
import org.springframework.stereotype.Component

import java.util.concurrent.atomic.AtomicBoolean

@CompileStatic
@Slf4j
@Component
class BookDeleter {

	/**
	   Scenario for "should generate tests triggered by a message":
	     client side: if sends a message to input.messageFrom then message will be sent to output.messageFrom
	     server side: will send a message to input, verify the message contents and await upon receiving message on the output messageFrom
	 */
	void bookDeleted(Exchange exchange) {
		BookDeleted bookDeleted = exchange.in.getBody(BookDeleted)
		log.info("Deleting book [$bookDeleted]")
		bookSuccessfulyDeleted.set(true)
		log.info("Book successfuly deleted [$bookSuccessfulyDeleted]")
	}

	AtomicBoolean bookSuccessfulyDeleted = new AtomicBoolean(false)
}
