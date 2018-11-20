package org.springframework.cloud.contract.verifier.dsl.wiremock

import spock.lang.Ignore
import spock.lang.Specification

import org.springframework.cloud.contract.spec.Contract
import org.springframework.cloud.contract.verifier.file.ContractMetadata

/**
 * @author Olga Maciaszek-Sharma
 */
class WireMockXmlSpec extends Specification implements WireMockStubVerifier {

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

	@Ignore // FIXME
	def should_generate_stubs_with_request_body_matchers() {
		given:
			Contract contractDsl = Contract.make {
				request {
					method 'GET'
					urlPath '/get'
					body """
<test>
<duck>123</duck>
<alpha>abc</alpha>
<number>123</number>
<aBoolean>true</aBoolean>
<date>2017-01-01</date>
<dateTime>2017-01-01T01:23:45</dateTime>
<time>01:02:34</time>
<valueWithoutAMatcher>foo</valueWithoutAMatcher>
<valueWithTypeMatch>string</valueWithTypeMatch>
</test>"""
					// FIXME
					bodyMatchers {
						jsonPath('$.duck', byRegex("[0-9]{3}"))
						jsonPath('$.duck', byEquality())
						jsonPath('$.alpha', byRegex(onlyAlphaUnicode()))
						jsonPath('$.alpha', byEquality())
						jsonPath('$.number', byRegex(number()))
						jsonPath('$.aBoolean', byRegex(anyBoolean()))
						jsonPath('$.date', byDate())
						jsonPath('$.dateTime', byTimestamp())
						jsonPath('$.time', byTime())
//						jsonPath("\$.['key'].['complex.key']", byEquality())
					}
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
	}

}

