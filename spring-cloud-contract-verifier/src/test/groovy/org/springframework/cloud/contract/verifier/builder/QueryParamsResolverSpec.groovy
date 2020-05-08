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

package org.springframework.cloud.contract.verifier.builder

import org.springframework.cloud.contract.spec.internal.DslProperty
import org.springframework.cloud.contract.spec.internal.MatchingStrategy
import org.springframework.cloud.contract.spec.internal.OptionalProperty
import org.springframework.cloud.contract.spec.internal.QueryParameter
import org.springframework.cloud.contract.spec.internal.RegexProperty
import spock.lang.Specification

import java.util.regex.Pattern

class QueryParamsResolverSpec extends Specification {

	def 'should return serverValue for QueryParameter'() {
		given:
			Object parameter = new QueryParameter("some_param", dslProperty)
		when:
			String resolved = new QueryParamsResolver() {}.resolveParamValue(parameter)
		then:
			resolved == expected
		where:
			dslProperty                                 | expected
			new DslProperty<String>("client", "server") | "server"
			new DslProperty<String>(null, "server")     | "server"
			new DslProperty<String>("client", null)     | "null"
			new DslProperty<String>(null, null)         | "null"
	}

	def 'should return optionalPattern for OptionalProperty'() {
		given:
			Object parameter = new OptionalProperty(value)
		when:
			String resolved = new QueryParamsResolver() {}.resolveParamValue(parameter)
		then:
			resolved == expected
		where:
			value                                          | expected
			"server"                                       | "(server)?"
			null                                           | "()?"
			new RegexProperty(Pattern.compile(".*"))       | "(.*)?"
			new RegexProperty(null, Pattern.compile(".*")) | "(.*)?"
			new RegexProperty(Pattern.compile(".*"), null) | "(.*)?"
			new RegexProperty("", Pattern.compile(".*"))   | "(.*)?"
			new RegexProperty(Pattern.compile(".*"), "")   | "(.*)?"
	}

	def 'should return serverValue for MatchingStrategy'() {
		given:
			Object parameter = new MatchingStrategy(value, MatchingStrategy.Type.EQUAL_TO)
		when:
			String resolved = new QueryParamsResolver() {}.resolveParamValue(parameter)
		then:
			resolved == expected
		where:
			value                                       | expected
			"server"                                    | "server"
			null                                        | "null"
			new DslProperty<String>("client", "server") | "server"
			new DslProperty<String>(null, "server")     | "server"
			new DslProperty<String>("client", null)     | "null"
			new DslProperty<String>(null, null)         | "null"
	}

	def 'should return serverValue for DslProperty'() {
		when:
			String resolved = new QueryParamsResolver() {}.resolveParamValue(parameter)
		then:
			resolved == expected
		where:
			parameter                                   | expected
			new DslProperty<String>("client", "server") | "server"
			new DslProperty<String>(null, "server")     | "server"
			new DslProperty<String>("client", null)     | "null"
			new DslProperty<String>(null, null)         | "null"
	}

	def 'should return \"null\" for null'() {
		when:
			String resolved = new QueryParamsResolver() {}.resolveParamValue(null)
		then:
			resolved == "null"
	}

}
