package io.codearte.accurest.stubrunner.messaging.camel

import com.jayway.jsonpath.DocumentContext
import com.jayway.jsonpath.JsonPath
import com.toomuchcoding.jsonassert.JsonAssertion
import com.toomuchcoding.jsonassert.JsonVerifiable
import groovy.transform.PackageScope
import io.codearte.accurest.dsl.GroovyDsl
import io.codearte.accurest.messaging.AccurestObjectMapper
import io.codearte.accurest.util.JsonPaths
import io.codearte.accurest.util.JsonToJsonPathsConverter
import org.apache.camel.Exchange
import org.apache.camel.Predicate

import java.util.regex.Pattern

/**
 * Passes through a message that matches the one defined in the DSL
 *
 * @author Marcin Grzejszczak
 */
@PackageScope
class StubRunnerCamelPredicate implements Predicate {

	private final GroovyDsl groovyDsl
	private final AccurestObjectMapper objectMapper = new AccurestObjectMapper()

	StubRunnerCamelPredicate(GroovyDsl groovyDsl) {
		this.groovyDsl = groovyDsl
	}

	@Override
	boolean matches(Exchange exchange) {
		if(!headersMatch(exchange)){
			return false
		}
		Object inputMessage = exchange.in.body
		JsonPaths jsonPaths = JsonToJsonPathsConverter.transformToJsonPathWithStubsSideValues(groovyDsl.input.messageBody)
		DocumentContext parsedJson = JsonPath.parse(objectMapper.writeValueAsString(inputMessage))
		return jsonPaths.every { matchesJsonPath(parsedJson, it) }
	}

	private boolean matchesJsonPath(DocumentContext parsedJson, JsonVerifiable jsonVerifiable) {
		try {
			JsonAssertion.assertThat(parsedJson).matchesJsonPath(jsonVerifiable.jsonPath())
			return true
		} catch (Exception e) {
			return false
		}
	}

	private boolean headersMatch(Exchange exchange) {
		Map<String, Object> headers = exchange.getIn().getHeaders()
		return groovyDsl.input.messageHeaders.entries.every {
			String name = it.name
			Object value = it.clientValue
			Object valueInHeader = headers.get(name)
			return value instanceof Pattern ?
					value.matcher(valueInHeader.toString()).matches() :
					valueInHeader == value
		}
	}
}
