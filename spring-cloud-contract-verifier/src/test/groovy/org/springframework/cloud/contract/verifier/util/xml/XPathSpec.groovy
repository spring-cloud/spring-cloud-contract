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

package org.springframework.cloud.contract.verifier.util.xml

import spock.lang.Specification
import spock.lang.Unroll

/**
 * @author Marcin Grzejszczak
 */
class XPathSpec extends Specification {

	@Unroll
	def "should generate [#expectedXPath] for XPath [#xPath]"() {
		expect:
			xPath == expectedXPath
		where:
			xPath                                                                                                            || expectedXPath
			XPathBuilder.builder().node("some").node("nested").node("anothervalue").isEqualTo(4).xPath()                     || '''/some/nested[anothervalue=4]'''
			XPathBuilder.builder().node("some").node("nested").array("withlist").contains("name").isEqualTo("name1").xPath() || '''/some/nested/withlist[name='name1']'''
			XPathBuilder.builder().node("some").node("nested").array("withlist").contains("name").isEqualTo("name2").xPath() || '''/some/nested/withlist[name='name2']'''
			XPathBuilder.builder().node("some").node("nested").node("json").isEqualTo("with \"val'ue").xPath()               || '''/some/nested[json=concat('with "val',"'",'ue')]'''
			XPathBuilder.builder().node("some", "nested", "json").isEqualTo("with \"val'ue").xPath()                         || '''/some/nested[json=concat('with "val',"'",'ue')]'''
	}

}
