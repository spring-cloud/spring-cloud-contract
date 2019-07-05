/*
 * Copyright 2013-2019 the original author or authors.
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
