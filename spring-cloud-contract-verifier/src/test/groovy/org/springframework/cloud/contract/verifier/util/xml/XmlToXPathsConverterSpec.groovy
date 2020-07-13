package org.springframework.cloud.contract.verifier.util.xml

import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import javax.xml.xpath.XPathExpressionException

class XmlToXPathsConverterSpec extends Specification {
	@Shared
	String namedXml = '''<ns1:customer xmlns:ns1="http://demo.com/testns">
      <email>customer@test.com</email>
    </ns1:customer>
    '''
	@Shared
	String unnamedXml = '''<customer>
      <email>customer@test.com</email>
    </customer>
    '''

	@Unroll
	def "should generate [#expectedValue] for xPath [#value]"() {
		expect:
		value == expectedValue
		where:
		value                                                                                || expectedValue
		XmlToXPathsConverter.retrieveValueFromBody("/ns1:customer/email/text()", namedXml)   || '''customer@test.com'''
		XmlToXPathsConverter.retrieveValueFromBody("/customer/email/text()", namedXml)       || ''''''
		XmlToXPathsConverter.retrieveValueFromBody("/customer/email/text()", unnamedXml)     || '''customer@test.com'''
	}

	@Unroll
	def "should throw exception when searching for inexistent name space"() {
		when:
		XmlToXPathsConverter.retrieveValueFromBody("/ns1:customer/email/text()", unnamedXml)
		then:
		def e = thrown(XPathExpressionException)
		e.message.contains('Prefix must resolve to a namespace: ns1')
	}
}
