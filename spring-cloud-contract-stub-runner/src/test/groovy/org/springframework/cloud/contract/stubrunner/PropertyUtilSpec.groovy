/*
 * Copyright 2013-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.contract.stubrunner

import spock.lang.Specification

class PropertyUtilSpec extends Specification {

	def "should return [#expectedResult] when queried for [#queriedProp] and system is [#systemProperty] and env [#envVariable]"() {
		given:
			def sysProp = systemProperty
			def envVar = envVariable
			def expectedEnv = expectedEnvVar
			PropertyFetcher fetcher = new PropertyFetcher() {
				@Override
				String systemProp(String prop) {
					return sysProp
				}

				@Override
				String envVar(String prop) {
					assert prop == expectedEnv
					return envVar
				}
			}
			PropertyUtil.FETCHER = fetcher
		expect:
			expectedResult == PropertyUtil.isPropertySet(queriedProp)
		where:
			queriedProp   | systemProperty | envVariable | expectedEnvVar | expectedResult
			"foo.bar-baz" | null           | null        | "FOO_BAR_BAZ"  | false
			"foo.bar-baz" | null           | "true"      | "FOO_BAR_BAZ"  | true
			"foo.bar-baz" | null           | "false"     | "FOO_BAR_BAZ"  | false
			"foo.bar-baz" | "false"        | "true"      | "FOO_BAR_BAZ"  | false
			"foo.bar-baz" | "true"         | "true"      | "FOO_BAR_BAZ"  | true
	}
}
