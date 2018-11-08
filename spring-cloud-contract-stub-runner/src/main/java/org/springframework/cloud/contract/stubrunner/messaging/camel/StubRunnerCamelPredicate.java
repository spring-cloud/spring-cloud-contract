/*
 *  Copyright 2013-2017 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.springframework.cloud.contract.stubrunner.messaging.camel;

import java.util.Map;
import java.util.regex.Pattern;

import org.apache.camel.Exchange;
import org.apache.camel.Predicate;
import org.springframework.cloud.contract.spec.Contract;
import org.springframework.cloud.contract.spec.internal.BodyMatcher;
import org.springframework.cloud.contract.spec.internal.BodyMatchers;
import org.springframework.cloud.contract.spec.internal.Header;
import org.springframework.cloud.contract.verifier.util.MapConverter;
import org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierObjectMapper;
import org.springframework.cloud.contract.verifier.util.JsonPaths;
import org.springframework.cloud.contract.verifier.util.JsonToJsonPathsConverter;
import org.springframework.cloud.contract.verifier.util.MethodBufferingJsonVerifiable;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.toomuchcoding.jsonassert.JsonAssertion;

/**
 * Passes through a message that matches the one defined in the DSL
 *
 * @author Marcin Grzejszczak
 */
class StubRunnerCamelPredicate implements Predicate {

	private final Contract groovyDsl;
	private final ContractVerifierObjectMapper objectMapper = new ContractVerifierObjectMapper();

	public StubRunnerCamelPredicate(Contract groovyDsl) {
		this.groovyDsl = groovyDsl;
	}

	@Override
	public boolean matches(Exchange exchange) {
		if (!headersMatch(exchange.getIn().getHeaders())) {
			return false;
		}
		Object inputMessage = exchange.getIn().getBody();
		BodyMatchers matchers = this.groovyDsl.getInput().getBodyMatchers();
		Object dslBody = MapConverter.getStubSideValues(this.groovyDsl.getInput().getMessageBody());

		DocumentContext parsedJson = deserialize(inputMessage);
		return matchMessage(matchers, dslBody, parsedJson);
	}

	private boolean matchMessage(BodyMatchers matchers, Object dslBody, DocumentContext parsedJson) {
		boolean matches = true;
		JsonPaths jsonPaths = getJsonPaths(matchers, dslBody);
		for (MethodBufferingJsonVerifiable path : jsonPaths) {
			matches &= matchesJsonPath(parsedJson, path.jsonPath());
		}
		if (matchers != null && matchers.hasMatchers()) {
			for (BodyMatcher matcher : matchers.jsonPathMatchers()) {
				String jsonPath = JsonToJsonPathsConverter.convertJsonPathAndRegexToAJsonPath(matcher, dslBody);
				matches &= matchesJsonPath(parsedJson, jsonPath);
			}
		}
		return matches;
	}

	private JsonPaths getJsonPaths(BodyMatchers matchers, Object dslBody) {
		Object matchingInputMessage = JsonToJsonPathsConverter
				.removeMatchingJsonPaths(dslBody, matchers);
		return JsonToJsonPathsConverter
				.transformToJsonPathWithStubsSideValuesAndNoArraySizeCheck(
						matchingInputMessage);
	}

	private DocumentContext deserialize(Object inputMessage) {
		DocumentContext parsedJson;
		try {
			parsedJson = JsonPath
					.parse(this.objectMapper.writeValueAsString(inputMessage));
		}
		catch (JsonProcessingException e) {
			throw new IllegalStateException("Cannot serialize to JSON", e);
		}
		return parsedJson;
	}

	private boolean matchesJsonPath(DocumentContext parsedJson, String jsonPath) {
		try {
			JsonAssertion.assertThat(parsedJson).matchesJsonPath(jsonPath);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	private boolean headersMatch(Map<String, Object> headers) {
		boolean matches = true;
		for (Header it : this.groovyDsl.getInput().getMessageHeaders().getEntries()) {
			String name = it.getName();
			Object value = it.getClientValue();
			Object valueInHeader = headers.get(name);
			matches &= matchValue(value, valueInHeader);
		}
		return matches;
	}

	private boolean matchValue(Object value, Object valueInHeader) {
		return value instanceof Pattern ?
				((Pattern) value).matcher(valueInHeader.toString()).matches() :
				valueInHeader!=null && valueInHeader.equals(value);
	}
}
