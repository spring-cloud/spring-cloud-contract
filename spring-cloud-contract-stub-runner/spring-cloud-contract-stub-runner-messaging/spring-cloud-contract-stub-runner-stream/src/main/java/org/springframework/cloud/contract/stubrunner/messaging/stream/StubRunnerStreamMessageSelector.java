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
import org.springframework.cloud.contract.spec.internal.Header;
import org.springframework.cloud.contract.verifier.messaging.ContractVerifierObjectMapper;
import org.springframework.cloud.contract.verifier.util.JsonPaths;
import org.springframework.cloud.contract.verifier.util.JsonToJsonPathsConverter;
import org.springframework.cloud.contract.verifier.util.MethodBufferingJsonVerifiable;
import org.springframework.integration.core.MessageSelector;
import org.springframework.messaging.Message;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.toomuchcoding.jsonassert.JsonAssertion;
import com.toomuchcoding.jsonassert.JsonVerifiable;

/**
 * Passes through a message that matches the one defined in the DSL
 *
 * @author Marcin Grzejszczak
 */
public class StubRunnerStreamMessageSelector implements MessageSelector {

	private final Contract groovyDsl;
	private final ContractVerifierObjectMapper objectMapper = new ContractVerifierObjectMapper();

	StubRunnerStreamMessageSelector(Contract groovyDsl) {
		this.groovyDsl = groovyDsl;
	}

	@Override
	public boolean accept(Message<?> message) {
		if(!headersMatch(message)){
			return false;
		}
		Object inputMessage = message.getPayload();
		JsonPaths jsonPaths = JsonToJsonPathsConverter
				.transformToJsonPathWithStubsSideValues(
						groovyDsl.getInput().getMessageBody());
		DocumentContext parsedJson;
		try {
			parsedJson = JsonPath.parse(objectMapper.writeValueAsString(inputMessage));
			for (MethodBufferingJsonVerifiable it : jsonPaths) {
				if (!matchesJsonPath(parsedJson, it)) {
					return false;
				}
			}
		}
		catch (JsonProcessingException e) {
			throw new IllegalStateException("Cannot parse JSON", e);
		}
		return true;
	}

	private boolean matchesJsonPath(DocumentContext parsedJson, JsonVerifiable jsonVerifiable) {
		try {
			JsonAssertion.assertThat(parsedJson).matchesJsonPath(jsonVerifiable.jsonPath());
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	private boolean headersMatch(Message<?> message) {
		Map<String, Object> headers = message.getHeaders();
		for (Header it : groovyDsl.getInput().getMessageHeaders().getEntries()) {
			String name = it.getName();
			Object value = it.getClientValue();
			Object valueInHeader = headers.get(name);
			boolean matches = true;
			if (value instanceof Pattern) {
				Pattern pattern = (Pattern) value;
				matches = pattern.matcher(valueInHeader.toString()).matches();
			} else {
				matches = valueInHeader!=null && valueInHeader.equals(value);
			}
			if (!matches) {
				return matches;
			}
		}
		return true;
	}
}
