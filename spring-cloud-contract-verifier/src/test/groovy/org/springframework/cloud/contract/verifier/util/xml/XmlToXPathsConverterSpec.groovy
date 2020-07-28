package org.springframework.cloud.contract.verifier.util.xml

import org.springframework.cloud.contract.spec.internal.BodyMatchers
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
		value                                                                              || expectedValue
		XmlToXPathsConverter.retrieveValueFromBody("/ns1:customer/email/text()", namedXml) || '''customer@test.com'''
		XmlToXPathsConverter.retrieveValueFromBody("/customer/email/text()", namedXml)     || ''''''
		XmlToXPathsConverter.retrieveValueFromBody("/customer/email/text()", unnamedXml)   || '''customer@test.com'''
	}

	@Unroll
	def "should throw exception when searching for in existent name space"() {
		when:
		XmlToXPathsConverter.retrieveValueFromBody("/ns1:customer/email/text()", unnamedXml)
		then:
		def e = thrown(XPathExpressionException)
		e.message.contains('Prefix must resolve to a namespace: ns1')
	}

	@Unroll
	def "should remove elements to [#expectedValue] for xPath [#value]"() {
		given:
		BodyMatchers m = new BodyMatchers()
		m.xPath(xpath, m.byEquality());
		expect:
		result == XmlToXPathsConverter.removeMatchingXPaths(xml, m)
		where:
		xpath                        || xml        || result
		"/ns1:customer/email/text()" || namedXml   || '''<?xml version="1.0" encoding="UTF-8" standalone="no"?><ns1:customer xmlns:ns1="http://demo.com/testns">\n      <email/>\n    </ns1:customer>'''
		"/customer/email/text()"     || namedXml   || '''<?xml version="1.0" encoding="UTF-8" standalone="no"?><ns1:customer xmlns:ns1="http://demo.com/testns">\n      <email>customer@test.com</email>\n    </ns1:customer>'''
		"/customer/email/text()"     || unnamedXml || '''<?xml version="1.0" encoding="UTF-8" standalone="no"?><customer>\n      <email/>\n    </customer>'''
	}
}
