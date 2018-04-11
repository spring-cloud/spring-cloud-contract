package org.springframework.cloud.contract.spec.internal

import spock.lang.Specification

/**
 * @author Marcin Grzejszczak
 */
class CookiesSpec extends Specification {

	Cookies cookies = cookie()

	def "should convert cookies to a stub side map"() {
		expect:
			cookies.asStubSideMap() == [foo: "client", bar: "client"]
	}

	def "should convert cookies to a test side map"() {
		expect:
			cookies.asTestSideMap() == [foo: "server", bar: "server"]
	}

	private Cookies cookie() {
		Cookies cookies = new Cookies()
		cookies.cookie("foo", new DslProperty("client", "server"))
		cookies.cookie(["bar": new DslProperty<>("client", "server")])
		return cookies
	}
}
