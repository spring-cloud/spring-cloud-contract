package org.springframework.cloud.contract.verifier.util.xml

import javax.xml.xpath.XPathExpressionException

import spock.lang.Issue
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import org.springframework.cloud.contract.spec.internal.BodyMatchers

/**
 * @author Chris Bono
 * @since 2.1.0
 */
class XmlToXPathsConverterSpec extends Specification {
	@Shared
	String namedXml = '''<ns1:customer xmlns:ns1="http://demo.com/testns">
      <email>customer@test.com</email>
    </ns1:customer>
    '''
	@Shared
	String namedDefaultNamespaceXml = '''<customer xmlns="http://demo.com/testns">
      <email>customer@test.com</email>
    </customer>
    '''
	@Shared
	String unnamedXml = '''<customer>
      <email>customer@test.com</email>
    </customer>
    '''
	@Shared
	String attributesInChildXml = '''<customer first_custom_attribute="first_value">
      <email second_custom_attribute="second_value" >customer@test.com</email>
      <address third_custom_attribute="third_value"/>
    </customer>
    '''

	@Unroll
	def "should generate [#expectedValue] for xPath [#value]"() {
		expect:
			value == expectedValue
		where:
			value                                                                                                                                                                           || expectedValue
			XmlToXPathsConverter.retrieveValueFromBody("/customer/email/text()", unnamedXml)                                                                                                || '''customer@test.com'''
			XmlToXPathsConverter.retrieveValueFromBody("/ns1:customer/email/text()", namedXml)                                                                                              || '''customer@test.com'''
			XmlToXPathsConverter.retrieveValueFromBody("//email/text()", namedXml)                                                                                                          || '''customer@test.com'''
			XmlToXPathsConverter.retrieveValueFromBody("/customer/email/text()", namedXml)                                                                                                  || ''''''
			XmlToXPathsConverter.retrieveValueFromBody("/*[local-name()='customer' and namespace-uri()='http://demo.com/testns']/*[local-name()='email']/text()", namedDefaultNamespaceXml) || '''customer@test.com'''
			XmlToXPathsConverter.retrieveValueFromBody("//*[local-name()='email']/text()", namedDefaultNamespaceXml)                                                                        || '''customer@test.com'''
			XmlToXPathsConverter.retrieveValueFromBody("/*[local-name()='customer']/*[local-name()='email']/text()", namedDefaultNamespaceXml)                                              || '''customer@test.com'''
			XmlToXPathsConverter.retrieveValueFromBody("/*[local-name()='customer' and namespace-uri()='http://demo.com/testns']/email/text()", namedDefaultNamespaceXml)                   || ''''''
			XmlToXPathsConverter.retrieveValueFromBody("/customer/email/text()", namedDefaultNamespaceXml)                                                                                  || ''''''
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
	def "should generate matched path [#expectedValue] for xPath [#value]"() {
		expect:
			value == expectedValue
		where:
			value                                                                  || expectedValue
			XmlToXPathsConverter.mapToMatchers(attributesInChildXml).get(0).path() || '''/customer/email/text()'''
			XmlToXPathsConverter.mapToMatchers(attributesInChildXml).get(1).path() || '''/customer/@first_custom_attribute'''
			XmlToXPathsConverter.mapToMatchers(attributesInChildXml).get(2).path() || '''/customer/email/@second_custom_attribute'''
			XmlToXPathsConverter.mapToMatchers(attributesInChildXml).get(3).path() || '''/customer/address/@third_custom_attribute'''
	}

	@Unroll
	def "should remove elements to [#result] for xPath [#xpath]"() {
		given:
			BodyMatchers m = new BodyMatchers()
			m.xPath(xpath, m.byEquality())
		expect:
			result == XmlToXPathsConverter.removeMatchingXPaths(xml, m)
		where:
			xpath                        || xml        || result
			"/customer/email/text()"     || unnamedXml || '''<?xml version="1.0" encoding="UTF-8" standalone="no"?><customer>\n      <email/>\n    </customer>'''
			"/customer/email/text()"     || namedXml   || '''<?xml version="1.0" encoding="UTF-8" standalone="no"?><ns1:customer xmlns:ns1="http://demo.com/testns">\n      <email>customer@test.com</email>\n    </ns1:customer>'''
			"/ns1:customer/email/text()" || namedXml   || '''<?xml version="1.0" encoding="UTF-8" standalone="no"?><ns1:customer xmlns:ns1="http://demo.com/testns">\n      <email/>\n    </ns1:customer>'''
			"/*[local-name()='customer' and namespace-uri()='http://demo.com/testns']/*[local-name()='email']/text()" || namedDefaultNamespaceXml  || '''<?xml version="1.0" encoding="UTF-8" standalone="no"?><customer xmlns="http://demo.com/testns">\n      <email/>\n    </customer>'''
	}

	@Issue("#1546")
	def "should remove multiple elements when xpath matches them"() {
		given:
			String test = '''\
<root>
    <childOne>
        <id>123</id>
    </childOne>
    <childTwo>
        <id>234</id>
    </childTwo>
</root>
'''
			BodyMatchers m = new BodyMatchers()
			m.xPath("/root/*/id/text()", m.byEquality())
		expect:
			XmlToXPathsConverter.removeMatchingXPaths(test, m) == '''<?xml version="1.0" encoding="UTF-8" standalone="no"?><root>\n    <childOne>\n        <id/>\n    </childOne>\n    <childTwo>\n        <id/>\n    </childTwo>\n</root>'''
	}
}
