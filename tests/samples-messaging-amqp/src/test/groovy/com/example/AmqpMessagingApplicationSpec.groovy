/*
 * Copyright 2013-2019 the original author or authors.
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

package com.example

import javax.inject.Inject

import com.jayway.jsonpath.DocumentContext
import com.jayway.jsonpath.JsonPath
import com.toomuchcoding.jsonassert.JsonAssertion
import groovy.json.JsonOutput
import spock.lang.Issue
import spock.lang.Specification

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootContextLoader
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.contract.spec.Contract
import org.springframework.cloud.contract.verifier.messaging.boot.AutoConfigureMessageVerifier
import org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierMessage
import org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierMessaging
import org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierObjectMapper
import org.springframework.test.context.ContextConfiguration

import static com.toomuchcoding.jsonassert.JsonAssertion.assertThatJson
import static org.assertj.core.api.Assertions.assertThat
import static org.springframework.cloud.contract.verifier.messaging.util.ContractVerifierMessagingUtil.headers

// Context configuration would end up in base class
@ContextConfiguration(classes = [AmqpMessagingApplication], loader = SpringBootContextLoader)
@AutoConfigureMessageVerifier
@SpringBootTest(properties = "stubrunner.amqp.enabled=true")
class AmqpMessagingApplicationSpec extends Specification {

	// ALL CASES
	@Inject
	ContractVerifierMessaging contractVerifierMessaging
	@Inject
	ContractVerifierObjectMapper contractVerifierObjectMapper

	def "should work for triggered based messaging"() {
		given:
			def dsl = Contract.make {
				// Human readable description
				description 'Some description'
				// Label by means of which the output message can be triggered
				label 'some_label'
				// input to the contract
				input {
					// the contract will be triggered by a method
					triggeredBy('publishBook()')
				}
				// output message of the contract
				outputMessage {
					// destination to which the output message will be sent
					sentTo('test-exchange')
					// the body of the output message
					body('''{ "name" : "some" }''')
					// the headers of the output message
					headers {
						header('contentType', 'application/json')
						header('__TypeId__', 'com.example.Book')
					}
				}
			}
			// generated test should look like this:
		when:
			publishBook()
		then:
			def response = contractVerifierMessaging.receive('test-exchange')
			response.headers.get('contentType') == 'application/json'
		and:
			DocumentContext parsedJson = JsonPath.
					parse(contractVerifierObjectMapper.writeValueAsString(response.payload))
			JsonAssertion.assertThat(parsedJson).field('name').isEqualTo('some')
	}

	@Issue("332")
	def "should work for second scenario"() {
		given:
			def dsl =
					Contract.make {
						description("""
Represents scenario 2 from documentation: 
http://cloud.spring.io/spring-cloud-contract/spring-cloud-contract.html#_publisher_side_test_generation

"The input message triggers an output message."

```
given:
	rabbit service is running
when:
	input message is received
then:
	message is send
```

""")
						label 'some_label2'
						input {
							messageFrom('input')
							messageBody([
									name: 'foo2'
							])
							messageHeaders {
								messagingContentType(applicationJson())
								header('amqp_replyTo', 'amq.rabbitmq.reply-to')
								header('bill', 'bill')
							}
						}

						outputMessage {
							sentTo('')
							body('''{ "name" : "foo2" }''')
							headers {
								messagingContentType(applicationJson())
							}
						}
					}
			// generated test should look like this:
		and:
			ContractVerifierMessage inputMessage = contractVerifierMessaging.create(
					"{\"name\":\"foo2\"}"
					, headers()
					.header("contentType", "application/json")
					.header("amqp_replyTo", "amq.rabbitmq.reply-to")
					.header("bill", "bill")
			)
		when:
			contractVerifierMessaging.send(inputMessage, "input")
		then:
			ContractVerifierMessage response = contractVerifierMessaging.receive("")
			assertThat(response).isNotNull()
			assertThat(response.getHeader("contentType")).isNotNull()
			assertThat(response.getHeader("contentType").toString()).
					isEqualTo("application/json")
		and:
			DocumentContext parsedJson = JsonPath.parse(contractVerifierObjectMapper.
					writeValueAsString(response.getPayload()))
			assertThatJson(parsedJson).field("['name']").isEqualTo("foo2")
	}

	@Issue("178")
	def "should work for input/output when bytes are used"() {
		given:
			def inputBody = [
					ratedItemId: "992e46d8-ab05-4a26-a740-6ef7b0daeab3",
					eventType  : "CREATED"
			]
			def dsl = Contract.make {
				label 'ratedItem-no-metricid'
				input {
					messageFrom("rated-item-service.rated-item-event.exchange")
					messageHeaders {
						header("X-tenant", "1234")
						header("contentType", "application/json")
					}
					messageBody(inputBody)
				}
				outputMessage {
					sentTo('bill-service.rated-item-event.retry-exchange')
					body(
							ratedItemId: "992e46d8-ab05-4a26-a740-6ef7b0daeab3",
							eventType: "CREATED"
					)
				}
			}
		when:
			contractVerifierMessaging.send(contractVerifierMessaging.
					create(new JsonOutput().toJson(inputBody), [
							"X-tenant"   : "1234",
							"contentType": "application/json"
					]), "rated-item-service.rated-item-event.exchange")
		then:
			def response = contractVerifierMessaging.
					receive('bill-service.rated-item-event.retry-exchange')
		and:
			DocumentContext parsedJson = JsonPath.
					parse(contractVerifierObjectMapper.writeValueAsString(response.payload))
			JsonAssertion.assertThat(parsedJson).field('ratedItemId').
					isEqualTo('992e46d8-ab05-4a26-a740-6ef7b0daeab3')
			JsonAssertion.assertThat(parsedJson).field('eventType').isEqualTo('CREATED')
	}

	// BASE CLASS WOULD HAVE THIS:

	@Autowired
	MessagePublisher messagePublisher

	void publishBook() {
		this.messagePublisher.sendMessage(new Book("some"))
	}
}
