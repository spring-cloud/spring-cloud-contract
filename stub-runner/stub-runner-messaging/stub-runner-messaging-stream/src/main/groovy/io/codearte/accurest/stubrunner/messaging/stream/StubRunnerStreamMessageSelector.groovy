package io.codearte.accurest.stubrunner.messaging.stream

import com.fasterxml.jackson.databind.ObjectMapper
import com.jayway.jsonpath.DocumentContext
import com.jayway.jsonpath.JsonPath
import com.toomuchcoding.jsonassert.JsonAssertion
import com.toomuchcoding.jsonassert.JsonVerifiable
import groovy.transform.CompileStatic
import io.codearte.accurest.dsl.GroovyDsl
import io.codearte.accurest.util.JsonPaths
import io.codearte.accurest.util.JsonToJsonPathsConverter
import org.springframework.integration.core.MessageSelector
import org.springframework.messaging.Message

import java.util.regex.Pattern

/**
 * Passes through a message that matches the one defined in the DSL
 *
 * @author Marcin Grzejszczak
 */
@CompileStatic
class StubRunnerStreamMessageSelector implements MessageSelector {

	private final GroovyDsl groovyDsl
	private final ObjectMapper objectMapper = new ObjectMapper()

	StubRunnerStreamMessageSelector(GroovyDsl groovyDsl) {
		this.groovyDsl = groovyDsl
	}

	@Override
	boolean accept(Message<?> message) {
		if(!headersMatch(message)){
			return false
		}
		Object inputMessage = message.getPayload()
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

	private boolean headersMatch(Message message) {
		Map<String, Object> headers = message.getHeaders()
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
