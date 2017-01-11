/*
 *  Copyright 2013-2016 the original author or authors.
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

package org.springframework.cloud.contract.stubrunner.messaging.stream;

import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.cloud.contract.spec.Contract;
import org.springframework.cloud.contract.spec.internal.BodyMatcher;
import org.springframework.cloud.contract.spec.internal.BodyMatchers;
import org.springframework.cloud.contract.spec.internal.Header;
import org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierObjectMapper;
import org.springframework.cloud.contract.verifier.util.JsonPaths;
import org.springframework.cloud.contract.verifier.util.JsonToJsonPathsConverter;
import org.springframework.cloud.contract.verifier.util.MapConverter;
import org.springframework.cloud.contract.verifier.util.MethodBufferingJsonVerifiable;
import org.springframework.integration.core.MessageSelector;
import org.springframework.messaging.Message;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.toomuchcoding.jsonassert.JsonAssertion;

/**
 * Passes through a message that matches the one defined in the DSL
 *
 * @author Marcin Grzejszczak
 */
class StubRunnerStreamMessageSelector implements MessageSelector {

	private final Contract groovyDsl;
	private final ContractVerifierObjectMapper objectMapper = new ContractVerifierObjectMapper();

	StubRunnerStreamMessageSelector(Contract groovyDsl) {
		this.groovyDsl = groovyDsl;
	}

	@Override
	public boolean accept(Message<?> message) {
		if (!headersMatch(message)) {
			return false;
		}
		Object inputMessage = message.getPayload();
		BodyMatchers matchers = this.groovyDsl.getInput().getMatchers();
		Object dslBody = MapConverter.getStubSideValues(this.groovyDsl.getInput().getMessageBody());
		Object matchingInputMessage = JsonToJsonPathsConverter
				.removeMatchingJsonPaths(dslBody, matchers);
		JsonPaths jsonPaths = JsonToJsonPathsConverter
				.transformToJsonPathWithStubsSideValuesAndNoArraySizeCheck(
						matchingInputMessage);
		DocumentContext parsedJson;
		try {
			parsedJson = JsonPath.parse(this.objectMapper.writeValueAsString(inputMessage));
		}
		catch (JsonProcessingException e) {
			throw new IllegalStateException("Cannot serialize to JSON", e);
		}
		boolean matches = true;
		for (MethodBufferingJsonVerifiable path : jsonPaths) {
			matches &= matchesJsonPath(parsedJson, path.jsonPath());
		}
		if (matchers != null && matchers.hasMatchers()) {
			for (BodyMatcher matcher : matchers.jsonPathMatchers()) {
				String jsonPath = JsonToJsonPathsConverter.convertJsonPathAndRegexToAJsonPath(matcher);
				matches &= matchesJsonPath(parsedJson, jsonPath);
			}
		}
		return matches;
	}

	private boolean matchesJsonPath(DocumentContext parsedJson, String jsonPath) {
		try {
			JsonAssertion.assertThat(parsedJson).matchesJsonPath(jsonPath);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	private boolean headersMatch(Message<?> message) {
		Map<String, Object> headers = message.getHeaders();
		for (Header it : this.groovyDsl.getInput().getMessageHeaders().getEntries()) {
			String name = it.getName();
			Object value = it.getClientValue();
			Object valueInHeader = headers.get(name);
			boolean matches;
			if (value instanceof Pattern) {
				Pattern pattern = (Pattern) value;
				matches = pattern.matcher(valueInHeader.toString()).matches();
			} else {
				matches = valueInHeader!=null && valueInHeader.equals(value);
			}
			if (!matches) {
				return false;
			}
		}
		return true;
	}
}
