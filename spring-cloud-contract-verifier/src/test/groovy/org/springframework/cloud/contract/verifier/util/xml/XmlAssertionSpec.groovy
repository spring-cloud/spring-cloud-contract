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

import groovy.xml.MarkupBuilder
import spock.lang.Issue
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

class XmlAssertionSpec extends Specification {

	@Shared
	String xml1 = '''<?xml version="1.0" encoding="UTF-8" ?>
    <some>
        <nested>
            <json>with &quot;val&apos;ue</json>
            <anothervalue>4</anothervalue>
            <withattr id="a" id2="b">foo</withattr>
            <withlist>
                <name>name1</name>
            </withlist>
            <withlist>
                <name>name2</name>
            </withlist>
            <withlist>
                8
            </withlist>
            <withlist>
                <name id="10" surname="kowalski">name3</name>
            </withlist>
        </nested>
    </some>'''

	@Unroll
	def 'should convert an xml with a map as root to a map of path to value '() {
		expect:
			verifiable.xPath() == expectedXPath
		where:
			verifiable                                                             || expectedXPath
			XmlAssertion.assertThat(xml1).node("some").
					node("nested").node("anothervalue").isEqualTo(4)               || '''/some/nested[anothervalue=4]'''
			XmlAssertion.assertThat(xml1).node("some").
					node("nested").node("anothervalue")                            || '''/some/nested/anothervalue'''
			XmlAssertion.
					assertThat(xml1).node("some").text()                           || '''/some/text()'''
			XmlAssertion.assertThat(xml1).node("some").
					node("nested").node("withattr").withAttribute("id", "a").
					withAttribute("id2", "b")                                      || '''/some/nested/withattr[@id='a'][@id2='b']'''
			XmlAssertion.assertThat(xml1).node("some").
					node("nested").node("withattr").withAttribute("id")            || '''/some/nested/withattr/@id'''
			XmlAssertion.assertThat(xml1).node("some").
					node("nested").node("withattr").isEqualTo("foo").
					withAttribute("id", "a").withAttribute("id2", "b")             || '''/some/nested[withattr='foo']/withattr[@id='a'][@id2='b']'''
			XmlAssertion.assertThatXml(xml1).node("some").
					node("nested").node("anothervalue").isEqualTo(4)               || '''/some/nested[anothervalue=4]'''
			XmlAssertion.assertThat(xml1).node("some").
					node("nested").array("withlist").contains("name").
					isEqualTo("name1")                                             || '''/some/nested/withlist[name='name1']'''
			XmlAssertion.assertThat(xml1).node("some").
					node("nested").array("withlist").contains("name").
					isEqualTo("name2")                                             || '''/some/nested/withlist[name='name2']'''
			XmlAssertion.assertThat(xml1).node("some").
					node("nested").array("withlist").contains("name").isEqualTo("name3").
					withAttribute("id", "10").withAttribute("surname", "kowalski") || '''/some/nested/withlist[name='name3']/name[@id='10'][@surname='kowalski']'''
			XmlAssertion.assertThat(xml1).node("some").
					node("nested").array("withlist").isEqualTo(8)                  || '''/some/nested/withlist[number()=8]'''
			XmlAssertion.assertThat(xml1).node("some").
					node("nested").node("json").isEqualTo("with \"val'ue")         || '''/some/nested[json=concat('with "val',"'",'ue')]'''
			XmlAssertion.assertThat(xml1).
					node("some", "nested", "json").isEqualTo("with \"val'ue")      || '''/some/nested[json=concat('with "val',"'",'ue')]'''
	}

	@Shared
	String xml2 = '''<?xml version="1.0" encoding="UTF-8" ?>
    <root>
        <property1>a</property1>
        <property2>b</property2>
    </root>
'''

	@Unroll
	def "should generate assertions for simple response body"() {
		expect:
			verifiable.xPath() == expectedXPath
		where:
			verifiable                               || expectedXPath
			XmlAssertion.assertThat(xml2).node("root").
					node("property1").isEqualTo("a") || '''/root[property1='a']'''
			XmlAssertion.assertThat(xml2).node("root").
					node("property2").isEqualTo("b") || '''/root[property2='b']'''
	}

	@Shared
	String xml3 = '''<?xml version="1.0" encoding="UTF-8" ?>
    <root>
        <property1>true</property1>
        <property2 />
        <property3>false</property3>
        <property4>5</property4>
    </root>
'''

	@Unroll
	def "should generate assertions for null and boolean values"() {
		expect:
			verifiable.xPath() == expectedXPath
		where:
			verifiable                                  || expectedXPath
			XmlAssertion.assertThat(xml3).node("root").
					node("property1").isEqualTo("true") || '''/root[property1='true']'''
			XmlAssertion.assertThat(xml3).node("root").
					node("property2").isNull()          || '''not(boolean(/root/property2/text()[1]))'''
			XmlAssertion.assertThat(xml3).node("root").
					node("property3").isEqualTo(false)  || '''/root[property3='false']'''
			XmlAssertion.assertThat(xml3).node("root").
					node("property4").isEqualTo(5)      || '''/root[property4=5]'''
	}

	@Shared
	StringWriter xml4 = new StringWriter()
	@Shared
	def root4 = new MarkupBuilder(xml4).root {
		property1('a')
		property2 {
			a('sth')
			b('sthElse')
		}
	}

	@Unroll
	def "should generate assertions for simple response body constructed from map with a list"() {
		expect:
			verifiable.xPath() == expectedXPath
		where:
			verifiable                   || expectedXPath
			XmlAssertion.assertThat(xml4.toString()).
					node("root").node("property1").
					isEqualTo("a")       || '''/root[property1='a']'''
			XmlAssertion.assertThat(xml4.toString()).
					node("root").array("property2").contains("a").
					isEqualTo("sth")     || '''/root/property2[a='sth']'''
			XmlAssertion.assertThat(xml4.toString()).
					node("root").array("property2").contains("b").
					isEqualTo("sthElse") || '''/root/property2[b='sthElse']'''
	}

	@Shared
	String xml7 = '''<?xml version="1.0" encoding="UTF-8" ?>
    <root>
        <property1>
            <property2>test1</property2>
        </property1>
        <property1>
            <property3>test2</property3>
        </property1>
    </root>
'''

	@Unroll
	def "should generate assertions for array inside response body element"() {
		expect:
			verifiable.xPath() == expectedXPath
		where:
			verifiable                                                          || expectedXPath
			XmlAssertion.assertThat(xml7).node("root").
					array("property1").contains("property2").isEqualTo("test1") || '''/root/property1[property2='test1']'''
			XmlAssertion.assertThat(xml7).node("root").
					array("property1").contains("property3").isEqualTo("test2") || '''/root/property1[property3='test2']'''
	}

	@Shared
	String xml8 = """<?xml version="1.0" encoding="UTF-8" ?>
    <root>
        <property1>a</property1>
        <property2>
            <property3>b</property3>
        </property2>
    </root>
"""

	def "should generate assertions for nested objects in response body"() {
		expect:
			verifiable.xPath() == expectedXPath
		where:
			verifiable                                                 || expectedXPath
			XmlAssertion.assertThat(xml8).node("root").
					node("property2").node("property3").isEqualTo("b") || '''/root/property2[property3='b']'''
			XmlAssertion.assertThat(xml8).node("root").
					node("property1").isEqualTo("a")                   || '''/root[property1='a']'''
	}

	@Shared
	StringWriter xml9 = new StringWriter()
	@Shared
	def root9 = new MarkupBuilder(xml9).root {
		property1('a')
		property2(123)
	}

	@Unroll
	def "should generate regex assertions for map objects in response body"() {
		expect:
			verifiable.xPath() == expectedXPath
		where:
			verifiable                                                 || expectedXPath
			XmlAssertion.assertThat(xml9.toString()).
					node("root").node("property2").matches("[0-9]{3}") || '''/root[matches(property2, '[0-9]{3}')]'''
			XmlAssertion.assertThat(xml9.toString()).
					node("root").node("property1").isEqualTo("a")      || '''/root[property1='a']'''
	}

	def "should generate escaped regex assertions for string objects in response body"() {
		given:
			StringWriter xml = new StringWriter()
			def root = new MarkupBuilder(xml).root {
				property2(123123)
			}
		expect:
			def verifiable = XmlAssertion.
					assertThat(xml.toString()).node("root").node("property2").
					matches("\\d+")
			verifiable.xPath() == '''/root[matches(property2, '\\d+')]'''
	}

	@Shared
	StringWriter xml10 = new StringWriter()
	@Shared
	def root10 = new MarkupBuilder(xml10).root {
		errors {
			property('bank_account_number')
			message('incorrect_format')
		}
	}

	@Unroll
	def "should work with more complex stuff and xpaths"() {
		expect:
			verifiable.xPath() == expectedXPath
		where:
			verifiable                               || expectedXPath
			XmlAssertion.assertThat(xml10.toString()).
					node("root").array("errors").contains("property").
					isEqualTo("bank_account_number") || '''/root/errors[property='bank_account_number']'''
			XmlAssertion.assertThat(xml10.toString()).
					node("root").array("errors").contains("message").
					isEqualTo("incorrect_format")    || '''/root/errors[message='incorrect_format']'''
	}

	@Shared
	String xml11 = '''<?xml version="1.0" encoding="UTF-8" ?>
    <root>
        <place>
            <bounding_box>
                <coordinates>-77.119759</coordinates>
                <coordinates>38.995548</coordinates>
                <coordinates>-76.909393</coordinates>
                <coordinates>38.791645</coordinates>
            </bounding_box>
        </place>
    </root>
'''

	@Unroll
	def "should manage to parse a double array"() {
		expect:
			verifiable.xPath() == expectedXPath
		where:
			verifiable                    || expectedXPath
			XmlAssertion.assertThat(xml11).node("root").
					node("place").node("bounding_box").array("coordinates").
					isEqualTo(38.995548)  || '''/root/place/bounding_box/coordinates[number()=38.995548]'''
			XmlAssertion.assertThat(xml11).node("root").
					node("place").node("bounding_box").array("coordinates").
					isEqualTo(-77.119759) || '''/root/place/bounding_box/coordinates[number()=-77.119759]'''
			XmlAssertion.assertThat(xml11).node("root").
					node("place").node("bounding_box").array("coordinates").
					isEqualTo(-76.909393) || '''/root/place/bounding_box/coordinates[number()=-76.909393]'''
			XmlAssertion.assertThat(xml11).node("root").
					node("place").node("bounding_box").array("coordinates").
					isEqualTo(38.791645)  || '''/root/place/bounding_box/coordinates[number()=38.791645]'''

	}

	def "should run XPath when provided manually"() {
		given:
			String xml = """<?xml version="1.0" encoding="UTF-8" ?>
    <root>
        <property1>a</property1>
        <property2>
            <property3>b</property3>
        </property2>
    </root>
"""
		and:
			String xPath = '''/root/property2[property3='b']'''
		expect:
			XmlAssertion.assertThat(xml).matchesXPath(xPath)
	}

	def "should throw exception when XPath is not matched"() {
		given:
			String xml = """<?xml version="1.0" encoding="UTF-8" ?>
    <root>
        <property1>a</property1>
        <property2>
            <property3>b</property3>
        </property2>
    </root>
"""
		and:
			String xPath = '''/root/property2[property3='non-existing']'''
		when:
			XmlAssertion.assertThat(xml).matchesXPath(xPath)
		then:
			IllegalStateException illegalStateException = thrown(IllegalStateException)
			illegalStateException.message.contains("Parsed XML")
			illegalStateException.message.contains("doesn't match the XPath")
	}

	def "should not throw exception when json path is not matched and system prop overrides the check"() {
		given:
			String xml = """<?xml version="1.0" encoding="UTF-8" ?>
    <root>
        <property1>a</property1>
        <property2>
            <property3>b</property3>
        </property2>
    </root>
    """
		and:
			String xPath = '''/root/property2[property3='non-existing']'''
		when:
			XmlAssertion.assertThat(xml).
					withoutThrowingException().matchesXPath(xPath)
		then:
			noExceptionThrown()
	}

	def "should generate escaped regex assertions for text with regular expression values"() {
		given:
			// '"<>[]()
			String xml = """<?xml version="1.0" encoding="UTF-8" ?>
        <root>
            <property1>&apos;&quot;&lt;&gt;[]()</property1>
        </root>"""
		expect:
			def verifiable = XmlAssertion.assertThat(xml).
					node("root").node("property1").matches('\'"<>\\[\\]\\(\\)')
			verifiable.xPath() == '''/root[matches(property1, concat('',"'",'"<>\\[\\]\\(\\)'))]'''
	}

	def "should escape regular expression properly"() {
		given:
			String xml = """<?xml version="1.0" encoding="UTF-8" ?>
            <root>
                <path>/api/12</path>
                <correlationId>123456</correlationId>
            </root>"""
		expect:
			def verifiable = XmlAssertion.assertThatXml(xml).
					node("root").node("path").matches("^/api/[0-9]{2}\$")
			verifiable.xPath() == '''/root[matches(path, '^/api/[0-9]{2}$')]'''
	}

	def "should escape single quotes in a quoted string"() {
		given:
			String xml = """<?xml version="1.0" encoding="UTF-8" ?>
        <root>
            <text>text with &apos;quotes&apos; inside</text>
        </root>
            """
		expect:
			def verifiable = XmlAssertion.assertThatXml(xml).
					node("root").node("text").isEqualTo("text with 'quotes' inside")
			verifiable.xPath() == '''/root[text=concat('text with ',"'",'quotes',"'",' inside')]'''
	}

	def "should escape brackets in a string"() {
		given:
			String xml = """<?xml version="1.0" encoding="UTF-8" ?>
        <root>
            <id>&lt;escape me&gt;</id>
        </root>
            """
		expect:
			def verifiable = XmlAssertion.assertThatXml(xml).
					node("root").node("id").isEqualTo("<escape me>")
			verifiable.xPath() == '''/root[id='<escape me>']'''
	}

	def "should escape double quotes in a quoted string"() {
		given:
			String xml = """<?xml version="1.0" encoding="UTF-8" ?>
        <root>
            <text>text with &quot;quotes&quot; inside</text>
        </root>
            """
		expect:
			def verifiable = XmlAssertion.assertThatXml(xml).
					node("root").node("text").isEqualTo('''text with "quotes" inside''')
			verifiable.xPath() == '''/root[text='text with "quotes" inside']'''
	}

	def 'should resolve the value of XML via XPath'() {
		given:
			String xml =
					'''<?xml version="1.0" encoding="UTF-8" ?>
    <root>
        <element>
            <some>
                <nested>
                    <json>with value</json>
                    <anothervalue>4</anothervalue>
                    <withlist>
                        <name>name1</name>
                    </withlist>
                    <withlist>
                        <name>name2</name>
                    </withlist>
                    <withlist>
                        <anothernested>
                            <name>name3</name>
                        </anothernested>
                    </withlist>
                </nested>
            </some>
        </element>
        <element>
            <someother>
                <nested>
                    <json>true</json>
                    <anothervalue>4</anothervalue>
                    <withlist>
                        <name>name1</name>
                    </withlist>
                    <withlist>
                        <name>name2</name>
                    </withlist>
                    <withlist2>a</withlist2>
                    <withlist2>b</withlist2>
                </nested>
            </someother>
        </element>
    </root>'''
		expect:
			XPathBuilder.builder(xml).node("root").
					array("element").node("some").node("nested").node("json").
					read() == 'with value'
			XPathBuilder.builder(xml).node("root").
					array("element").node("some").node("nested").node("anothervalue").
					read() == 4.toString()
			// assertThat(xml).node("root").array("element").node("some").node("nested").array("withlist").node("name").read() == ['name1', 'name2'].toString()
			//assertThat(xml).node("root").array("element").node("someother").node("nested").array("withlist2").read() == ['a', 'b'].toString()
			XmlAssertion.assertThat(xml).node("root").
					array("element").node("someother").node("nested").node("json").
					read() == true.toString()
	}

	def 'should match array containing an array of primitives'() {
		given:
			String xml = '''<?xml version="1.0" encoding="UTF-8" ?>
    <root>
        <first_name>existing</first_name>
        <elements>
            <partners>
                <role>AGENT</role>
                <payment_methods>BANK</payment_methods>
                <payment_methods>CASH</payment_methods>
            </partners>
        </elements>
    </root>
'''
		expect:
			def verifiable = XmlAssertion.assertThatXml(xml).
					node("root").array("elements").array("partners").
					contains("payment_methods").isEqualTo("BANK")
			verifiable.xPath() == '''/root/elements/partners[payment_methods='BANK']'''
	}

	def 'should match pattern in array'() {
		given:
			String xml = '''<?xml version="1.0" encoding="UTF-8" ?>
    <root>
        <authorities>ROLE_ADMIN</authorities>
    </root>
    '''

		expect:
			def verifiable = XmlAssertion.assertThatXml(xml).
					node("root").array("authorities").matches("^[a-zA-Z0-9_\\- ]+\$")
			verifiable.xPath() == '''/root/authorities[matches(text(), '^[a-zA-Z0-9_\\- ]+$')]'''
	}

	def 'should manage to parse array with string values'() {
		given:
			String xml = '''<?xml version="1.0" encoding="UTF-8" ?>
    <root>
        <some_list>name1</some_list>
        <some_list>name2</some_list>
    </root>'''

		expect:
			def v1 = XmlAssertion.assertThat(xml).
					node("root").array("some_list").isEqualTo("name1")
			def v2 = XmlAssertion.assertThat(xml).
					node("root").array("some_list").isEqualTo("name2")
		and:
			v1.xPath() == '''/root/some_list[text()='name1']'''
			v2.xPath() == '''/root/some_list[text()='name2']'''
	}

	@Issue("#2")
	def 'should allow nested calls with counting the elements size'() {
		given:
			String xml = '''<?xml version="1.0" encoding="UTF-8" ?>
    <root>
        <some_list>name1</some_list>
        <some_list>name2</some_list>
    </root>'''

		expect:
			def v1 = XmlAssertion.assertThat(xml).
					node("root").array("some_list").hasSize(2).isEqualTo("name1")
		and:
			v1.xPath() == '''/root/some_list[text()='name1']'''
	}

	@Issue("#2")
	def 'should count the elements size'() {
		given:
			String xml = '''<?xml version="1.0" encoding="UTF-8" ?>
    <root>
        <some_list>name1</some_list>
        <some_list>name2</some_list>
    </root>'''

		expect:
			def v1 = XmlAssertion.assertThat(xml).
					node("root").array("some_list").hasSize(2)
		and:
			v1.xPath() == '''count(/root/some_list)'''
	}

	@Issue("#2")
	def 'should throw exception if size is wrong'() {
		given:
			String xml = '''<?xml version="1.0" encoding="UTF-8" ?>
    <root>
        <some_list>name1</some_list>
        <some_list>name2</some_list>
    </root>'''

		when:
			XmlAssertion.assertThat(xml).node("root").
					array("some_list").hasSize(1)
		then:
			IllegalStateException e = thrown(IllegalStateException)
			e.message.
					contains("has size [2] and not [1] for XPath <count(/root/some_list)>")
	}

	@Issue("#2")
	def 'should return 0 if element is missing'() {
		given:
			String xml = '''<?xml version="1.0" encoding="UTF-8" ?>
    <root>
        <some_list>name1</some_list>
        <some_list>name2</some_list>
    </root>'''

		when:
			XmlAssertion.assertThat(xml).node("root").
					array("foo").hasSize(1)
		then:
			IllegalStateException e = thrown(IllegalStateException)
			e.message.contains("has size [0] and not [1] for XPath <count(/root/foo)>")
	}

}
