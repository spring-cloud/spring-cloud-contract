package io.codearte.accurest.builder

import io.codearte.accurest.dsl.GroovyDsl
import spock.lang.Specification
/**
 * Tests used for the documentation
 *
 * @author Marcin Grzejszczak
 */
class ContractHttpDocsSpec extends Specification {

	GroovyDsl httpDsl  =
		// tag::http_dsl[]
		io.codearte.accurest.dsl.GroovyDsl.make {
			// Definition of HTTP request part of the contract
			// (this can be a valid request or invalid depending
			// on type of contract being specified).
			request {
				//...
			}

			// Definition of HTTP response part of the contract
			// (a service implementing this contract should respond
			// with following response after receiving request
			// specified in "request" part above).
			response {
				//...
			}

			// Contract priority, which can be used for overriding
			// contracts (1 is highest). Priority is optional.
			priority 1
		}
	// end::http_dsl[]

	GroovyDsl request  =
		// tag::request[]
		io.codearte.accurest.dsl.GroovyDsl.make {
			request {
				// HTTP request method (GET/POST/PUT/DELETE).
				method 'GET'

				// Path component of request URL is specified as follows.
				urlPath('/users')
			}

			response {
				//...
			}
		}
		// end::request[]

	GroovyDsl url  =
		// tag::url[]
		io.codearte.accurest.dsl.GroovyDsl.make {
			request {
				method 'GET'

				// Specifying `url` and `urlPath` in one contract is illegal.
				url('http://localhost:8888/users')
			}

			response {
				//...
			}
		}
		// end::url[]

	GroovyDsl urlPaths  =
		// tag::urlpath[]
		io.codearte.accurest.dsl.GroovyDsl.make {
			request {
				//...

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
						parameter 'gender': value(stub(containing("[mf]")), server('mf'))

						// `matching` function tests parameter
						// against passed regular expression.
						parameter 'offset': value(stub(matching("[0-9]+")), server(123))

						// `notMatching` functions tests if parameter
						// does not match passed regular expression.
						parameter 'loginStartsWith': value(stub(notMatching(".{0,2}")), server(3))
					}
				}

				//...
			}

			response {
				//...
			}
		}
		// end::urlpath[]

	GroovyDsl headers  =
		// tag::headers[]
		io.codearte.accurest.dsl.GroovyDsl.make {
			request {
				//...

				// Each header is added in form `'Header-Name' : 'Header-Value'`.
				headers {
					header 'Content-Type': 'application/json'
				}

				//...
			}

			response {
				//...
			}
		}
		// end::headers[]

	GroovyDsl body  =
		// tag::body[]
		io.codearte.accurest.dsl.GroovyDsl.make {
			request {
				//...

				// JSON and XML formats of request body are supported.
				// Format will be determined from a header or body's content.
				body '''{ "login" : "john", "name": "John The Contract" }'''
			}

			response {
				//...
			}
		}
		// end::body[]

	GroovyDsl bodyAsXml  =
		// tag::bodyAsXml[]
		io.codearte.accurest.dsl.GroovyDsl.make {
			request {
				//...

				// In this case body will be formatted as XML.
				body equalToXml(
						'''<user><login>john</login><name>John The Contract</name></user>'''
				)
			}

			response {
				//...
			}
		}
		// end::bodyAsXml[]

	GroovyDsl response  =
		// tag::response[]
		io.codearte.accurest.dsl.GroovyDsl.make {
			request {
				//...
			}
			response {
				// Status code sent by the server
				// in response to request specified above.
				status 200
			}
		}
		// end::response[]

	GroovyDsl regex  =
		// tag::regex[]
		io.codearte.accurest.dsl.GroovyDsl.make {
			request {
				method('GET')
				url $(client(~/\/[0-9]{2}/), server('/12'))
			}
			response {
				status 200
				body(
						id: value(
								client('123'),
								server(regex('[0-9]+'))
						),
						surname: $(
								client('Kowalsky'),
								server('Lewandowski')
						),
						name: 'Jan',
						created: $(client('2014-02-02 12:23:43'), server(execute('currentDate(it)'))),
						correlationId: value(client('5d1f9fef-e0dc-4f3d-a7e4-72d2220dd827'),
								server(regex('[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}'))
						)
				)
				headers {
					header 'Content-Type': 'text/plain'
				}
			}
		}
		// end::regex[]

	GroovyDsl optionals  =
		// tag::optionals[]
		io.codearte.accurest.dsl.GroovyDsl.make {
			priority 1
			request {
				method 'POST'
				url '/users/password'
				headers {
					header 'Content-Type': 'application/json'
				}
				body(
						email: $(stub(optional(regex(email()))), test('abc@abc.com')),
						callback_url: $(stub(regex(hostname())), test('http://partners.com'))
				)
			}
			response {
				status 404
				headers {
					header 'Content-Type': 'application/json'
				}
				body(
						code: value(stub("123123"), test(optional("123123")))
				)
			}
		}
		// end::optionals[]

	def 'should convert dsl with optionals to proper Spock test'() {
		given:
			BlockBuilder blockBuilder = new BlockBuilder(" ")
			new MockMvcSpockMethodRequestProcessingBodyBuilder(optionals).appendTo(blockBuilder)
		expect:
		String expectedTest =
// tag::optionals_test[]
"""
 given:
  def request = given()
    .header('Content-Type', 'application/json')
    .body('''{"email":"abc@abc.com","callback_url":"http://partners.com"}''')

 when:
  def response = given().spec(request)
    .post("/users/password")

 then:
  response.statusCode == 404
  response.header('Content-Type')  == 'application/json'
 and:
  DocumentContext parsedJson = JsonPath.parse(response.body.asString())
  assertThatJson(parsedJson).field("code").matches("(123123)?")
"""
// end::optionals_test[]
		stripped(blockBuilder.toString()) == stripped(expectedTest)
	}

	GroovyDsl method  =
		// tag::method[]
		io.codearte.accurest.dsl.GroovyDsl.make {
			request {
				method 'PUT'
				url $(client(regex('^/api/[0-9]{2}$')), server('/api/12'))
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
				body (
						path: $(client('/api/12'), server(regex('^/api/[0-9]{2}$'))),
						correlationId: $(client('1223456'), server(execute('isProperCorrelationId($it)')))
				)
				status 200
			}
		}
		// end::method[]

	private String stripped(String string) {
		return string.stripMargin().stripIndent().replace('\t', '').replace('\n', '').replace(' ','')
	}
}
