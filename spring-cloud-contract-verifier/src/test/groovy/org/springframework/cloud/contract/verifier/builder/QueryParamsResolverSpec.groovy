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
