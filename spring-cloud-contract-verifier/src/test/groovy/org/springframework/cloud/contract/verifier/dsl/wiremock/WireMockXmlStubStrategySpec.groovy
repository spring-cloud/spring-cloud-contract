package org.springframework.cloud.contract.verifier.dsl.wiremock


import spock.lang.Specification

import org.springframework.cloud.contract.spec.Contract
import org.springframework.cloud.contract.verifier.file.ContractMetadata

/**
 * @author Olga Maciaszek-Sharma
 */
class WireMockXmlStubStrategySpec extends Specification implements WireMockStubVerifier {

	def 'should convert dsl to stub matching request by equality'() {
		given:
			Contract groovyDsl = Contract.make {
				request {
					method('POST')
					url $('/test')
					body """
<note>
<to>Tove</to>
<from>Jani</from>
<heading>Reminder</heading>
<body>See you soon!</body>
</note>
"""
				}
				response {
					status OK()
				}
			}
		when:
			String wireMockStub = new WireMockStubStrategy("Test",
					new ContractMetadata(null, false, 0, null,
							groovyDsl), groovyDsl).toWireMockClientStub()

		then:
			stubMappingIsValidWireMockStub(wireMockStub)
	}

	def should_generate_stubs_from_plain_xml() {
		given:
			Contract contractDsl = Contract.make {
				request {
					method 'GET'
					urlPath '/get'
					body """
<test>
<duck type='xtype'>123</duck>
<alpha>abc</alpha>
<list>
<elem>abc</elem>
<elem>def</elem>
<elem>ghi</elem>
</list>
<number>123</number>
<aBoolean>true</aBoolean>
<date>2017-01-01</date>
<dateTime>2017-01-01T01:23:45</dateTime>
<time>01:02:34</time>
<valueWithoutAMatcher>foo</valueWithoutAMatcher>
<valueWithTypeMatch>string</valueWithTypeMatch>
<key><complex>foo</complex></key>
</test>"""
					headers {
						contentType(applicationXml())
					}
				}
				response {
					status(OK())
					headers {
						contentType(applicationXml())
					}
				}
			}
		when:
			String wireMockStub = new WireMockStubStrategy("Test",
					new ContractMetadata(null, false, 0, null, contractDsl), contractDsl)
					.toWireMockClientStub()
		then:
			stubMappingIsValidWireMockStub(wireMockStub)
			wireMockStub
					.replaceAll("\n", "")
					.replaceAll(' ', '')
					.contains(
					"""
"bodyPatterns": [
    {
      "matchesXPath": {
        "expression": "/test/duck/text()",
        "equalTo": "123"
      }
    },
    {
      "matchesXPath": {
        "expression": "/test/alpha/text()",
        "equalTo": "abc"
      }
    },
    {
      "matchesXPath": {
        "expression": "/test/list/elem/text()",
        "equalTo": "abc"
      }
    },
    {
      "matchesXPath": {
        "expression": "/test/list/elem/text()",
        "equalTo": "def"
      }
    },
    {
      "matchesXPath": {
        "expression": "/test/list/elem/text()",
        "equalTo": "ghi"
      }
    },
    {
      "matchesXPath": {
        "expression": "/test/number/text()",
        "equalTo": "123"
      }
    },
    {
      "matchesXPath": {
        "expression": "/test/aBoolean/text()",
        "equalTo": "true"
      }
    },
    {
      "matchesXPath": {
        "expression": "/test/date/text()",
        "equalTo": "2017-01-01"
      }
    },
    {
      "matchesXPath": {
        "expression": "/test/dateTime/text()",
        "equalTo": "2017-01-01T01:23:45"
      }
    },
    {
      "matchesXPath": {
        "expression": "/test/time/text()",
        "equalTo": "01:02:34"
      }
    },
    {
      "matchesXPath": {
        "expression": "/test/valueWithoutAMatcher/text()",
        "equalTo": "foo"
      }
    },
    {
      "matchesXPath": {
        "expression": "/test/valueWithTypeMatch/text()",
        "equalTo": "string"
      }
    },
    {
      "matchesXPath": {
        "expression": "/test/key/complex/text()",
        "equalTo": "foo"
      }
    },
    {
      "matchesXPath": {
        "expression": "/test/duck/@type",
        "equalTo": "xtype"
      }
      }]
""".replaceAll("\n", "").replaceAll(' ', ''))
	}

	def should_generate_stubs_with_request_body_matchers() {
		given:
			Contract contractDsl = Contract.make {
				request {
					method 'GET'
					urlPath '/get'
					body """
<test>
<duck type='xtype'>123</duck>
<alpha>abc</alpha>
<number>123</number>
<aBoolean>true</aBoolean>
<date>2017-01-01</date>
<dateTime>2017-01-01T01:23:45</dateTime>
<time>01:02:34</time>
<valueWithoutAMatcher>foo</valueWithoutAMatcher>
<valueWithTypeMatch>string</valueWithTypeMatch>
<key><complex>foo</complex></key>
</test>"""
					// TODO: verify and handle escapes!
					bodyMatchers {
						xPath('/test/duck/text()', byRegex("[0-9]{3}"))
						xPath('/test/duck/text()', byEquality())
						xPath('/test/alpha/text()', byRegex(onlyAlphaUnicode()))
						xPath('/test/alpha/text()', byEquality())
						xPath('/test/number/text()', byRegex(number()))
						xPath('/test/aBoolean/text()', byRegex(anyBoolean()))
						xPath('/test/date/text()', byDate())
						xPath('/test/dateTime/text()', byTimestamp())
						xPath('/test/time/text()', byTime())
						xPath('/test/*/complex/text()', byEquality())
						xPath('/test/duck/@type', byEquality())
						xPath('/test/duck',
								byXmlEquality('<duck type=\'number\'>123</duck>'))
					}
					headers {
						contentType(applicationXml())
					}
				}
				response {
					status(OK())
					headers {
						contentType(applicationXml())
					}
				}
			}
		when:
			String wireMockStub = new WireMockStubStrategy("Test",
					new ContractMetadata(null, false, 0, null, contractDsl), contractDsl)
					.toWireMockClientStub()
		then:
			stubMappingIsValidWireMockStub(wireMockStub)
			wireMockStub.replaceAll("\n", '').replaceAll(' ', '')
					.contains("""
      matchesXPath" : {
        "expression" : "/test/duck/text()",
        "matches" : "[0-9]{3}"
      }
    }, {
      "matchesXPath" : {
        "expression" : "/test/duck/text()",
        "equalTo" : "123"
      }
    }, {
      "matchesXPath" : {
        "expression" : "/test/alpha/text()",
        "matches" : "[\\\\p{L}]*"
      }
    }, {
      "matchesXPath" : {
        "expression" : "/test/alpha/text()",
        "equalTo" : "abc"
      }
    }, {
      "matchesXPath" : {
        "expression" : "/test/number/text()",
        "matches" : "-?(\\\\d*\\\\.\\\\d+|\\\\d+)"
      }
    }, {
      "matchesXPath" : {
        "expression" : "/test/aBoolean/text()",
        "matches" : "(true|false)"
      }
    }, {
      "matchesXPath" : {
        "expression" : "/test/date/text()",
        "matches" : "(\\\\d\\\\d\\\\d\\\\d)-(0[1-9]|1[012])-(0[1-9]|[12][0-9]|3[01])"
      }
    }, {
      "matchesXPath" : {
        "expression" : "/test/dateTime/text()",
        "matches" : "([0-9]{4})-(1[0-2]|0[1-9])-(3[01]|0[1-9]|[12][0-9])T(2[0-3]|[01][0-9]):([0-5][0-9]):([0-5][0-9])"
      }
    }, {
      "matchesXPath" : {
        "expression" : "/test/time/text()",
        "matches" : "(2[0-3]|[01][0-9]):([0-5][0-9]):([0-5][0-9])"
      }
    }, {
      "matchesXPath" : {
        "expression" : "/test/*/complex/text()",
        "equalTo" : "foo"
      }
    }, {
      "matchesXPath" : {
        "expression" : "/test/duck/@type",
        "equalTo" : "xtype"
      }
    }, {
      "matchesXPath" : {
        "expression" : "/test/duck",
        "equalToXml" : "<duck type=\\"number\\">123</duck>\\n"
      }""".replaceAll("\n", "").replaceAll(' ', ''))
	}

	def should_generate_stubs_from_both_xml_and_body_matchers() {
		given:
			Contract contractDsl = Contract.make {
				request {
					method 'GET'
					urlPath '/get'
					body """
<test>
<duck type='xtype'>123</duck>
<alpha>abc</alpha>
<number>123</number>
</test>"""
					bodyMatchers {
						xPath('/test/duck/text()', byEquality())
						xPath('/test/number/text()', byRegex(number()))
					}
					headers {
						contentType(applicationXml())
					}
				}
				response {
					status(OK())
					headers {
						contentType(applicationXml())
					}
				}
			}
		when:
			String wireMockStub = new WireMockStubStrategy("Test",
					new ContractMetadata(null, false, 0, null, contractDsl), contractDsl)
					.toWireMockClientStub()
		then:
			stubMappingIsValidWireMockStub(wireMockStub)
			wireMockStub.replaceAll("\n", "")
					.replaceAll(' ', '')
					.contains("""
   "bodyPatterns" : [ {
      "matchesXPath": {
        "expression": "/test/alpha/text()",
        "equalTo": "abc"
      }
    }, {
      "matchesXPath": {
        "expression": "/test/duck/text()",
        "equalTo": "123"
      }
    }, {
      "matchesXPath": {
        "expression": "/test/number/text()",
        "matches" : "-?(\\\\d*\\\\.\\\\d+|\\\\d+)"
      }
    } 
    ]
}
""".replaceAll("\n", "").replaceAll(' ', ''))
	}
}

