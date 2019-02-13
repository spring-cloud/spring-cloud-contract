package org.springframework.cloud.contract.verifier.util

import org.springframework.cloud.contract.spec.internal.DslProperty
import org.springframework.cloud.contract.verifier.util.ContentUtils
import spock.lang.Specification
import org.xml.sax.helpers.DefaultHandler

/**
 * @author Marcin Grzejszczak
 * @author Konstantin Shevchuk
 */
class ContentUtilsSpec extends Specification {

	def "should return the stub side"() {
		given:
			DslProperty<String> dslProperty = new DslProperty<>("stub", "test")
		expect:
			"stub" == ContentUtils.GET_STUB_SIDE(dslProperty)
	}

	def "should return the test side"() {
		given:
			DslProperty<String> dslProperty = new DslProperty<>("stub", "test")
		expect:
			"test" == ContentUtils.GET_TEST_SIDE(dslProperty)
	}

    def "should return XmlSlurper with default error handler"() {
        given:
        XmlSlurper xmlSlurper = ContentUtils.getXmlSlurperWithDefaultErrorHandler()
        expect:
        xmlSlurper.getErrorHandler() instanceof DefaultHandler
    }
}
