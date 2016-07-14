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

package org.springframework.cloud.samples.book

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.SpringApplicationContextLoader
import org.springframework.cloud.contract.verifier.messaging.boot.AutoConfigureContractVerifierMessaging;
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification
import com.jayway.restassured.module.mockmvc.RestAssuredMockMvc

/**
 * @author Marcin Grzejszczak
 */
@ContextConfiguration(classes = [IntegrationMessagingApplication], loader = SpringApplicationContextLoader)
@AutoConfigureContractVerifierMessaging
abstract class MessagingBaseSpec extends Specification {

	// BASE CLASS WOULD HAVE THIS:

	@Autowired BookService bookService
	@Autowired BookListener bookListener

	def setup() {
  	RestAssuredMockMvc.standaloneSetup(new IntegrationMessagingApplication())
  }

	void bookReturnedTriggered() {
		bookService.returnBook(new BookReturned("foo"))
	}

	void bookWasDeleted() {
		assert bookListener.bookSuccessfulyDeleted.get()
	}
}
