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
import spock.util.environment.RestoreSystemProperties

class StubRunnerPropertyUtilsSpec extends Specification {

	@RestoreSystemProperties
	def "should return [#expectedResult] when checking if [#queriedProp] is set, and system is [#systemProperty] and env [#envVariable]"() {
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
					assert prop == expectedEnv || prop == "STUBRUNNER_PROPERTIES_" + expectedEnv
					return envVar
				}
			}
			StubRunnerPropertyUtils.FETCHER = fetcher
		expect:
			expectedResult == StubRunnerPropertyUtils.isPropertySet(queriedProp)
		where:
			queriedProp   | systemProperty | envVariable | expectedEnvVar | expectedResult
			"foo.bar-baz" | null           | null        | "FOO_BAR_BAZ"  | false
			"foo.bar-baz" | null           | "true"      | "FOO_BAR_BAZ"  | true
			"foo.bar-baz" | null           | "false"     | "FOO_BAR_BAZ"  | false
			"foo.bar-baz" | "false"        | "true"      | "FOO_BAR_BAZ"  | false
			"foo.bar-baz" | "true"         | "true"      | "FOO_BAR_BAZ"  | true
	}

	@RestoreSystemProperties
	def "should return [#expectedResult] when queried for [#queriedProp] and system is [#systemProperty] and env [#envVariable]"() {
		given:
			def sysProp = systemProperty
			def envVar = envVariable
			def checkedSysProp = assertedSystemProp
			def checkedEnvVar = assertedEnvVar
			PropertyFetcher fetcher = new PropertyFetcher() {
				@Override
				String systemProp(String prop) {
					assert prop == checkedSysProp || prop == checkedSysProp - "stubrunner.properties."
					return sysProp
				}

				@Override
				String envVar(String prop) {
					assert prop == checkedEnvVar || prop == checkedEnvVar - "STUBRUNNER_PROPERTIES_"
					return envVar
				}
			}
			StubRunnerPropertyUtils.FETCHER = fetcher
		expect:
			expectedResult == StubRunnerPropertyUtils.getProperty(map, queriedProp)
		where:
			queriedProp   | map                    | systemProperty | envVariable | expectedResult | assertedSystemProp                  | assertedEnvVar
			"foo.bar-baz" | ["foo.bar-baz": "faz"] | "ab"           | "bc"        | "faz"          | "stubrunner.properties.foo.bar-baz" | "STUBRUNNER_PROPERTIES_FOO_BAR_BAZ"
			"foo.bar-baz" | [:]                    | "ab"           | "bc"        | "ab"           | "stubrunner.properties.foo.bar-baz" | "STUBRUNNER_PROPERTIES_FOO_BAR_BAZ"
			"foo.bar-baz" | [:]                    | ""             | "bc"        | "bc"           | "stubrunner.properties.foo.bar-baz" | "STUBRUNNER_PROPERTIES_FOO_BAR_BAZ"
			"foo.bar-baz" | null                   | ""             | "bc"        | "bc"           | "stubrunner.properties.foo.bar-baz" | "STUBRUNNER_PROPERTIES_FOO_BAR_BAZ"
	}

	@RestoreSystemProperties
	def "should return [#expectedResult] when prop is set for [#queriedProp] and system is [#systemProperty] and env [#envVariable]"() {
		given:
			def sysProp = systemProperty
			def envVar = envVariable
			PropertyFetcher fetcher = new PropertyFetcher() {
				@Override
				String systemProp(String prop) {
					return sysProp
				}

				@Override
				String envVar(String prop) {
					return envVar
				}
			}
			StubRunnerPropertyUtils.FETCHER = fetcher
		expect:
			expectedResult == StubRunnerPropertyUtils.hasProperty(map, queriedProp)
		where:
			queriedProp   | map                    | systemProperty | envVariable | expectedResult
			"foo.bar-baz" | ["foo.bar-baz": "faz"] | "ab"           | "bc"        | true
			"foo.bar-baz" | [:]                    | "ab"           | "bc"        | true
			"foo.bar-baz" | [:]                    | ""             | "bc"        | true
			"foo.bar-baz" | null                   | ""             | "bc"        | true
			"foo.bar-baz" | null                   | null           | null        | false          
	}

	def cleanupSpec() {
		StubRunnerPropertyUtils.FETCHER = new PropertyFetcher()
	}
}
