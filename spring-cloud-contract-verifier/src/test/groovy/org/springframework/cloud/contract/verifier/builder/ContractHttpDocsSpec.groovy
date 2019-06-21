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

package org.springframework.cloud.contract.verifier.builder

import spock.lang.Shared
import spock.lang.Specification

import org.springframework.cloud.contract.verifier.config.ContractVerifierConfigProperties

/**
 * Tests used for the documentation
 *
 * @author Marcin Grzejszczak
 */
class ContractHttpDocsSpec extends Specification {

	@Shared
	ContractVerifierConfigProperties properties = new ContractVerifierConfigProperties(assertJsonSize: true)

	org.springframework.cloud.contract.spec.Contract httpDsl =
			// tag::http_dsl[]
			org.springframework.cloud.contract.spec.Contract.make {
				// Definition of HTTP request part of the contract
				// (this can be a valid request or invalid depending
				// on type of contract being specified).
				request {
					method GET()
					url "/foo"
					//...
				}

				// Definition of HTTP response part of the contract
				// (a service implementing this contract should respond
				// with following response after receiving request
				// specified in "request" part above).
				response {
					status 200
					//...
				}

				// Contract priority, which can be used for overriding
				// contracts (1 is highest). Priority is optional.
				priority 1
			}
	// end::http_dsl[]

	org.springframework.cloud.contract.spec.Contract request =
			// tag::request[]
			org.springframework.cloud.contract.spec.Contract.make {
				request {
					// HTTP request methodBuilder (GET/POST/PUT/DELETE).
					method 'GET'

					// Path component of request URL is specified as follows.
					urlPath('/users')
				}

				response {
					//...
					status 200
				}
			}
	// end::request[]

	org.springframework.cloud.contract.spec.Contract url =
			// tag::url[]
			org.springframework.cloud.contract.spec.Contract.make {
				request {
					method 'GET'

					// Specifying `url` and `urlPath` in one contract is illegal.
					url('http://localhost:8888/users')
				}

				response {
					//...
					status 200
				}
			}
	// end::url[]

	org.springframework.cloud.contract.spec.Contract urlPaths =
			// tag::urlpath[]
			org.springframework.cloud.contract.spec.Contract.make {
				request {
					//...
					method GET()

					urlPath('/users') {

						// Each parameter is specified in form
						// `'paramName' : paramValue` where parameter value
						// may be a simple literal or one of matcher functions,
						// all of which are used in this example.
						queryParameters {

							// If a simple literal is used as value
							// default matcher function is used (equalTo)
							parameter 'limit': 100

							// `equalTo` function simply compares passed value
							// using identity operator (==).
							parameter 'filter': equalTo("email")

							// `containing` function matches strings
							// that contains passed substring.
							parameter 'gender': value(consumer(containing("[mf]")), producer('mf'))

							// `matching` function tests parameter
							// against passed regular expression.
							parameter 'offset': value(consumer(matching("[0-9]+")), producer(123))

							// `notMatching` functions tests if parameter
							// does not match passed regular expression.
							parameter 'loginStartsWith': value(consumer(notMatching(".{0,2}")), producer(3))
						}
					}

					//...
				}

				response {
					//...
					status 200
				}
			}
	// end::urlpath[]

	org.springframework.cloud.contract.spec.Contract headers =
			// tag::headers[]
			org.springframework.cloud.contract.spec.Contract.make {
				request {
					//...
					method GET()
					url "/foo"

					// Each header is added in form `'Header-Name' : 'Header-Value'`.
					// there are also some helper methods
					headers {
						header 'key': 'value'
						contentType(applicationJson())
					}

					//...
				}

				response {
					//...
					status 200
				}
			}
	// end::headers[]

	org.springframework.cloud.contract.spec.Contract cookies =
			// tag::cookies[]
			org.springframework.cloud.contract.spec.Contract.make {
				request {
					//...
					method GET()
					url "/foo"

					// Each Cookies is added in form `'Cookie-Key' : 'Cookie-Value'`.
					// there are also some helper methods
					cookies {
						cookie 'key': 'value'
						cookie('another_key', 'another_value')
					}

					//...
				}

				response {
					//...
					status 200
				}
			}
	// end::cookies[]

	org.springframework.cloud.contract.spec.Contract body =
			// tag::body[]
			org.springframework.cloud.contract.spec.Contract.make {
				request {
					//...
					method GET()
					url "/foo"

					// Currently only JSON format of request body is supported.
					// Format will be determined from a header or body's content.
					body '''{ "login" : "john", "name": "John The Contract" }'''
				}

				response {
					//...
					status 200
				}
			}
	// end::body[]

	org.springframework.cloud.contract.spec.Contract bodyAsXml =
			// tag::bodyAsXml[]
			org.springframework.cloud.contract.spec.Contract.make {
				request {
					//...
					method GET()
					url "/foo"

					// In this case body will be formatted as XML.
					body equalToXml(
							'''<user><login>john</login><name>John The Contract</name></user>'''
					)
				}

				response {
					//...
					status 200
				}
			}
	// end::bodyAsXml[]

	org.springframework.cloud.contract.spec.Contract response =
			// tag::response[]
			org.springframework.cloud.contract.spec.Contract.make {
				request {
					//...
					method GET()
					url "/foo"
				}
				response {
					// Status code sent by the server
					// in response to request specified above.
					status OK()
				}
			}
	// end::response[]

	org.springframework.cloud.contract.spec.Contract regex =
			// tag::regex[]
			org.springframework.cloud.contract.spec.Contract.make {
				request {
					method('GET')
					url $(consumer(~/\/[0-9]{2}/), producer('/12'))
				}
				response {
					status OK()
					body(
							id: $(anyNumber()),
							surname: $(
									consumer('Kowalsky'),
									producer(regex('[a-zA-Z]+'))
							),
							name: 'Jan',
							created: $(consumer('2014-02-02 12:23:43'), producer(execute('currentDate(it)'))),
							correlationId: value(consumer('5d1f9fef-e0dc-4f3d-a7e4-72d2220dd827'),
									producer(regex('[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}'))
							)
					)
					headers {
						header 'Content-Type': 'text/plain'
					}
				}
			}
	// end::regex[]

	org.springframework.cloud.contract.spec.Contract optionals =
			// tag::optionals[]
			org.springframework.cloud.contract.spec.Contract.make {
				priority 1
				request {
					method 'POST'
					url '/users/password'
					headers {
						contentType(applicationJson())
					}
					body(
							email: $(consumer(optional(regex(email()))), producer('abc@abc.com')),
							callback_url: $(consumer(regex(hostname())), producer('https://partners.com'))
					)
				}
				response {
					status 404
					headers {
						header 'Content-Type': 'application/json'
					}
					body(
							code: value(consumer("123123"), producer(optional("123123")))
					)
				}
			}
	// end::optionals[]

	def 'should convert dsl with optionals to proper Spock test'() {
		given:
			BlockBuilder blockBuilder = new BlockBuilder(" ")
			new HttpSpockMethodRequestProcessingBodyBuilder(optionals, properties, new GeneratedClassDataForMethod(
					new SingleTestGenerator.GeneratedClassData("foo", "bar", new File(".").toPath()), "method"))
					.appendTo(blockBuilder)
		expect:
			String expectedTest =
// tag::optionals_test[]
					"""
 given:
  def request = given()
    .header("Content-Type", "application/json")
    .body('''{"email":"abc@abc.com","callback_url":"https://partners.com"}''')

 when:
  def response = given().spec(request)
    .post("/users/password")

 then:
  response.statusCode == 404
  response.header('Content-Type')  == 'application/json'
 and:
  DocumentContext parsedJson = JsonPath.parse(response.body.asString())
  assertThatJson(parsedJson).field("['code']").matches("(123123)?")
"""
// end::optionals_test[]
			stripped(blockBuilder.toString()) == stripped(expectedTest)
	}

	org.springframework.cloud.contract.spec.Contract method =
			// tag::methodBuilder[]
			org.springframework.cloud.contract.spec.Contract.make {
				request {
					method 'PUT'
					url $(consumer(regex('^/api/[0-9]{2}$')), producer('/api/12'))
					headers {
						header 'Content-Type': 'application/json'
					}
					body '''\
						[{
							"text": "Gonna see you at Warsaw"
						}]
					'''
				}
				response {
					body(
							path: $(consumer('/api/12'), producer(regex('^/api/[0-9]{2}$'))),
							correlationId: $(consumer('1223456'), producer(execute('isProperCorrelationId($it)')))
					)
					status OK()
				}
			}
	// end::methodBuilder[]

	private String stripped(String string) {
		return string.stripMargin().stripIndent().replace('\t', '').replace('\n', '').replace(' ', '')
	}
}
