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

package org.springframework.cloud.contract.verifier.wiremock

import org.springframework.cloud.contract.spec.Contract
import org.springframework.cloud.contract.verifier.dsl.wiremock.WireMockStubMapping
import org.springframework.cloud.contract.verifier.util.ContractVerifierDslConverter
import spock.lang.Specification

import java.util.regex.Pattern

class WireMockToDslConverterSpec extends Specification {

	def 'should produce a Groovy DSL from WireMock stub'() {
		given:
			String wireMockStub = '''\
{
	"request": {
		"method": "GET",
		"url": "/path",
		"headers" : {
			"Accept": {
				"matches": "text/.*"
			},
			"X-Custom-Header": {
				"contains": "2134"
			}
		}
	},
	"response": {
		"status": 200,
		"body": '{"id": { "value": "132" }, "surname": "Kowalsky", "name": "Jan", "created": "2014-02-02 12:23:43" }',
		"headers": {
			"Content-Type": "text/plain"
		}
	}
}
'''
		and:
			stubMappingIsValidWireMockStub(wireMockStub)
		and:
			Contract expectedGroovyDsl = Contract.make {
				request {
					method 'GET'
					url '/path'
					headers {
						header('Accept': $(
								consumer(regex('text/.*')),
								producer('text/plain')
						))
						header('X-Custom-Header': $(
								consumer(regex('^.*2134.*$')),
								producer('121345')
						))

					}
				}
				response {
					status 200
					body("""{
    "id": {
        "value": "132"
    },
    "surname": "Kowalsky",
    "name": "Jan",
    "created": "2014-02-02 12:23:43"
}""")
					headers {
						header 'Content-Type': 'text/plain'

					}
				}
			}
		when:
			String groovyDsl = WireMockToDslConverter.fromWireMockStub(wireMockStub)
		then:
			def a = ContractVerifierDslConverter.convert(
					"""org.springframework.cloud.contract.spec.Contract.make {
				$groovyDsl
			}""")
			def b = expectedGroovyDsl
			a == b
	}


	def 'should convert WireMock stub with response body containing JSON with escaped double quotes'() {
		given:
			String wireMockStub = '''\
{
	"request": {
		"method": "DELETE",
		"urlPattern": "1",
		"headers": {
			"Content-Type": {
				"equalTo": "application/vnd.mymoid-adapter.v2+json; charset=UTF-8"
			}
		}
	},
	"response": {
		"status": 200,
		"body": "{\\"status\\": \\"OK\\"}",
		"headers": {
			"Content-Type": "application/json"
		}
	}
}
'''
		and:
			stubMappingIsValidWireMockStub(wireMockStub)
		and:
			Contract expectedGroovyDsl = Contract.make {
				request {
					method 'DELETE'
					url $(consumer(~/1/), producer('1'))
					headers {
						header('Content-Type': 'application/vnd.mymoid-adapter.v2+json; charset=UTF-8')
					}
				}
				response {
					status 200
					body( """{
    "status": "OK"
}""")
					headers {
						header 'Content-Type': 'application/json'
					}
				}
			}
		when:
			String groovyDsl = WireMockToDslConverter.fromWireMockStub(wireMockStub)
		then:
			def a = ContractVerifierDslConverter.convert(
					"""org.springframework.cloud.contract.spec.Contract.make {
					$groovyDsl
				}""")
			def b = expectedGroovyDsl
			(a.request.url.clientValue as Pattern).pattern() == (b.request.url.clientValue as Pattern).pattern()
	}

	def 'should convert WireMock stub with response body containing integer'() {
		given:
			String wireMockStub = '''\
{
  "request": {
	"method": "POST",
	"url": "/charge/count",
	"headers": {
	  "Content-Type": {
		"equalTo": "application/vnd.creditcard-reporter.v1+json"
	  }
	}
  },
  "response": {
	"status": 200,
	"body": 200,
	"headers": {
	  "Content-Type": "application/json"
	}
  }
}
'''
		and:
			stubMappingIsValidWireMockStub(wireMockStub)
		and:
			Contract expectedGroovyDsl = Contract.make {
				request {
					method 'POST'
					url '/charge/count'
					headers {
						header('Content-Type': 'application/vnd.creditcard-reporter.v1+json')
					}
				}
				response {
					status 200
					body(200)
					headers {
						header 'Content-Type': 'application/json'

					}
				}
			}
		when:
			String groovyDsl = WireMockToDslConverter.fromWireMockStub(wireMockStub)
		then:
			ContractVerifierDslConverter.convert(
					"""org.springframework.cloud.contract.spec.Contract.make {
				$groovyDsl
			}""") == expectedGroovyDsl
	}

	def 'should convert WireMock stub with response body as a list'() {
		given:
			String wireMockStub = '''\
{
  "request": {
	"method": "POST",
	"url": "/charge/count",
	"headers": {
	  "Content-Type": {
		"equalTo": "application/vnd.creditcard-reporter.v1+json"
	  }
	}
  },
  "response": {
	"status": 200,
	"body": '[ {"a":1, "c":"3"}, "b", "a" ]',
	"headers": {
	  "Content-Type": "application/json"
	}
  }
}
'''
		and:
			stubMappingIsValidWireMockStub(wireMockStub)
		and:
			Contract expectedGroovyDsl = Contract.make {
				request {
					method 'POST'
					url '/charge/count'
					headers {
						header('Content-Type': 'application/vnd.creditcard-reporter.v1+json')
					}
				}
				response {
					status 200
					body( """[
    {
        "a": 1,
        "c": "3"
    },
    "b",
    "a"
]""")
					headers {
						header 'Content-Type': 'application/json'
					}
				}
			}
		when:
			String groovyDsl = WireMockToDslConverter.fromWireMockStub(wireMockStub)
		then:
			ContractVerifierDslConverter.convert(
					"""org.springframework.cloud.contract.spec.Contract.make {
				$groovyDsl
			}""") == expectedGroovyDsl
	}

	def 'should convert WireMock stub with response body containing a nested list'() {
		given:
			String wireMockStub = '''\
{
  "request": {
	"method": "POST",
	"url": "/charge/search?pageNumber=0&size=2147483647",
	"headers": {
	  "Content-Type": {
		"equalTo": "application/vnd.creditcard-reporter.v1+json"
	  }
	}
  },
  "response": {
	"status": 200,
	"body": '[{"amount":1.01, "name":"Name", "info":{"title":"title1", "payload":null}, "booleanvalue":true, "user":null}, {"amount":2.01, "name":"Name2", "info":{"title":"title2", "payload":null}, "booleanvalue":true, "user":null}]'
	}
}
'''
		and:
			stubMappingIsValidWireMockStub(wireMockStub)
		and:
			Contract expectedGroovyDsl = Contract.make {
				request {
					method 'POST'
					url '/charge/search?pageNumber=0&size=2147483647'
					headers {
						header('Content-Type': 'application/vnd.creditcard-reporter.v1+json')
					}
				}
				response {
					status 200
					body("""[
    {
        "amount": 1.01,
        "name": "Name",
        "info": {
            "title": "title1",
            "payload": null
        },
        "booleanvalue": true,
        "user": null
    },
    {
        "amount": 2.01,
        "name": "Name2",
        "info": {
            "title": "title2",
            "payload": null
        },
        "booleanvalue": true,
        "user": null
    }
]""")
				}
			}
		when:
			String groovyDsl = WireMockToDslConverter.fromWireMockStub(wireMockStub)
		then:
			ContractVerifierDslConverter.convert(
					"""org.springframework.cloud.contract.spec.Contract.make {
				$groovyDsl
			}""") == expectedGroovyDsl
	}

	def 'should convert WireMock stub with request body checking equality to Json'() {
		given:
			String wireMockStub = '''\
{
  "request": {
	"method": "POST",
	"url": "/test",
	"bodyPatterns": [{
		"equalTo": '{"property1":"abc", "property2":"2017-01", "property3":"666", "property4":1428566412}'
	}]
  },
  "response": {
	"status": 200
	}
}
'''
		and:
			stubMappingIsValidWireMockStub(wireMockStub)
		and:
			Contract expectedGroovyDsl = Contract.make {
				request {
					method 'POST'
					url '/test'
					body ('''{"property1":"abc", "property2":"2017-01", "property3":"666", "property4":1428566412}''')
				}
				response {
					status 200
				}
			}
		when:
			String groovyDsl = WireMockToDslConverter.fromWireMockStub(wireMockStub)
		then:
			Contract evaluatedGroovyDsl = ContractVerifierDslConverter.convert(
					"""org.springframework.cloud.contract.spec.Contract.make {
				$groovyDsl
			}""")
		and:
			evaluatedGroovyDsl == expectedGroovyDsl
	}

	def 'should convert WireMock stub with request body checking matching to Json'() {
		given:
			String wireMockStub = '''\
{
  "request": {
    "method": "POST",
    "url": "/test",
    "bodyPatterns": [{
        "matches": "1"
    }]
  },
  "response": {
    "status": 200
    }
}
'''
		and:
			stubMappingIsValidWireMockStub(wireMockStub)
		and:
			Contract expectedGroovyDsl = Contract.make {
				request {
					method 'POST'
					url '/test'
					body $(consumer(~/1/), producer('1'))
				}
				response {
					status 200
				}
			}
		when:
			String groovyDsl = WireMockToDslConverter.fromWireMockStub(wireMockStub)
		then:
			Contract evaluatedGroovyDsl = ContractVerifierDslConverter.convert(
					"""org.springframework.cloud.contract.spec.Contract.make {
				$groovyDsl
			}""")
		and:
			(evaluatedGroovyDsl.request.body.clientValue as Pattern).pattern() == (expectedGroovyDsl.request.body.clientValue as Pattern).pattern()
	}

	def 'should convert WireMock stub with request body with equalToJson'() {
		given:
			String wireMockStub = '''\
{
  "request" : {
	"url" : "/test",
	"method" : "POST",
	"bodyPatterns" : [ {
	  "equalToJson" : '{"pan":"4855141150107894", "expirationDate":"2017-01", "dcvx":"178"}',
	  "jsonCompareMode" : "LENIENT"
	} ]
  },
  "response" : {
	"status" : 200
  }
}
'''
		and:
			stubMappingIsValidWireMockStub(wireMockStub)
		and:
			Contract expectedGroovyDsl = Contract.make {
				request {
					method 'POST'
					url '/test'
					body '''{"pan":"4855141150107894", "expirationDate":"2017-01", "dcvx":"178"}'''
				}
				response {
					status 200
				}
			}
		when:
			String groovyDsl = WireMockToDslConverter.fromWireMockStub(wireMockStub)
		then:
			Contract evaluatedGroovyDsl = ContractVerifierDslConverter.convert(
					"""org.springframework.cloud.contract.spec.Contract.make {
				$groovyDsl
			}""")
		and:
			evaluatedGroovyDsl == expectedGroovyDsl
	}

	def 'should convert WireMock stub with request body with equalTo'() {
		given:
			String wireMockStub = '''\
			{
			  "request" : {
				"url" : "/test",
				"method" : "POST",
				"bodyPatterns" : [ {
				  "equalTo" : '{"pan":"4855141150107894", "expirationDate":"2017-01", "dcvx":"178"}'
				} ]
			  },
			  "response" : {
				"status" : 200
			  }
			}
			'''
		and:
			stubMappingIsValidWireMockStub(wireMockStub)
		and:
			Contract expectedGroovyDsl = Contract.make {
				request {
					method 'POST'
					url '/test'
					body '''{"pan":"4855141150107894", "expirationDate":"2017-01", "dcvx":"178"}'''
				}
				response {
					status 200
				}
			}
		when:
			String groovyDsl = WireMockToDslConverter.fromWireMockStub(wireMockStub)
		then:
			Contract evaluatedGroovyDsl = ContractVerifierDslConverter.convert(
					"""org.springframework.cloud.contract.spec.Contract.make {
					$groovyDsl
				}""")
		and:
			evaluatedGroovyDsl == expectedGroovyDsl
	}

	def 'should convert WireMock stub with request body with matches'() {
		given:
			String wireMockStub = '''\
			{
			  "request" : {
				"url" : "/test",
				"method" : "POST",
				"bodyPatterns" : [ {
				  "matches" : "1"
				} ]
			  },
			  "response" : {
				"status" : 200
			  }
			}
			'''
		and:
			stubMappingIsValidWireMockStub(wireMockStub)
		and:
			Contract expectedGroovyDsl = Contract.make {
				request {
					method 'POST'
					url '/test'
					body $(consumer(~/1/), producer('1'))
				}
				response {
					status 200
				}
			}
		when:
			String groovyDsl = WireMockToDslConverter.fromWireMockStub(wireMockStub)
		then:
			Contract evaluatedGroovyDsl = ContractVerifierDslConverter.convert(
					"""org.springframework.cloud.contract.spec.Contract.make {
					$groovyDsl
				}""")
		and:
			(evaluatedGroovyDsl.request.body.clientValue as Pattern).pattern() == (expectedGroovyDsl.request.body.clientValue as Pattern).pattern()
	}

	def 'should convert WireMock stub with priorities'() {
		given:
			String wireMockStub = '''\
			{
			  "priority" : 2,
			  "request" : {
				"url" : "/test",
				"method" : "POST"
			  },
			  "response" : {
				"status" : 200
			  }
			}
			'''
		and:
			stubMappingIsValidWireMockStub(wireMockStub)
		and:
			Contract expectedGroovyDsl = Contract.make {
				priority 2
				request {
					method 'POST'
					url '/test'
				}
				response {
					status 200
				}
			}
		when:
			String groovyDsl = WireMockToDslConverter.fromWireMockStub(wireMockStub)
		then:
			Contract evaluatedGroovyDsl = ContractVerifierDslConverter.convert(
					"""org.springframework.cloud.contract.spec.Contract.make {
					$groovyDsl
				}""")
		and:
			evaluatedGroovyDsl == expectedGroovyDsl
	}

	void stubMappingIsValidWireMockStub(String mappingDefinition) {
		WireMockStubMapping.buildFrom(mappingDefinition)
	}
}
