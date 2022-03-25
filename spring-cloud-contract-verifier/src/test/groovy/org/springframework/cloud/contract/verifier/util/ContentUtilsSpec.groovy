/*
 * Copyright 2013-2020 the original author or authors.
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

package org.springframework.cloud.contract.verifier.util


import org.springframework.cloud.contract.spec.internal.DslProperty
import spock.lang.Specification
import org.xml.sax.helpers.DefaultHandler
import groovy.xml.XmlSlurper

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
