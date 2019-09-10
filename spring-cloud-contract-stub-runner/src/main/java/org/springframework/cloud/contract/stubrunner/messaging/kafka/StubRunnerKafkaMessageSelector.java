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

package org.springframework.cloud.contract.stubrunner.messaging.kafka;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.regex.Pattern;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.toomuchcoding.jsonassert.JsonAssertion;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.cloud.contract.spec.Contract;
import org.springframework.cloud.contract.spec.internal.BodyMatcher;
import org.springframework.cloud.contract.spec.internal.BodyMatchers;
import org.springframework.cloud.contract.spec.internal.FromFileProperty;
import org.springframework.cloud.contract.spec.internal.Header;
import org.springframework.cloud.contract.spec.internal.RegexProperty;
import org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierObjectMapper;
import org.springframework.cloud.contract.verifier.util.ContentType;
import org.springframework.cloud.contract.verifier.util.ContentUtils;
import org.springframework.cloud.contract.verifier.util.JsonPaths;
import org.springframework.cloud.contract.verifier.util.JsonToJsonPathsConverter;
import org.springframework.cloud.contract.verifier.util.MapConverter;
import org.springframework.cloud.contract.verifier.util.MethodBufferingJsonVerifiable;
import org.springframework.messaging.Message;

/**
 * Passes through a message that matches the one defined in the DSL.
 *
 * @author Marcin Grzejszczak
 * @author Tim Ysewyn
 */
class StubRunnerKafkaMessageSelector {

	private static final Map<Message<?>, Contract> CACHE = Collections
			.synchronizedMap(new WeakHashMap<>());

	private static final Log log = LogFactory.getLog(StubRunnerKafkaMessageSelector.class);

	private final List<Contract> groovyDsls;

	private final ContractVerifierObjectMapper objectMapper = new ContractVerifierObjectMapper();

	StubRunnerKafkaMessageSelector(List<Contract> groovyDsls) {
		this.groovyDsls = groovyDsls;
	}

	Contract matchingContract(Message<?> message) {
		if (CACHE.containsKey(message)) {
			return CACHE.get(message);
		}
		Contract contract = getContract(message);
		if (contract != null) {
			CACHE.put(message, contract);
		}
		return contract;
	}

	void updateCache(Message<?> message, Contract contract) {
		CACHE.put(message, contract);
	}

	private Contract getContract(Message<?> message) {
		for (Contract groovyDsl : this.groovyDsls) {
			Contract contract = matchContract(message, groovyDsl);
			if (contract != null) {
				return contract;
			}
		}
		return null;
	}

	private Contract matchContract(Message<?> message, Contract groovyDsl) {
		List<String> unmatchedHeaders = headersMatch(message, groovyDsl);
		if (!unmatchedHeaders.isEmpty()) {
			if (log.isDebugEnabled()) {
				log.debug("Contract [" + groovyDsl
						+ "] hasn't matched the following headers " + unmatchedHeaders);
			}
			return null;
		}
		Object inputMessage = message.getPayload();
		Object dslBody = MapConverter
				.getStubSideValues(groovyDsl.getInput().getMessageBody());
		if (dslBody instanceof FromFileProperty) {
			if (log.isDebugEnabled()) {
				log.debug("Will compare file content");
			}
			FromFileProperty property = (FromFileProperty) dslBody;
			if (property.isString()) {
				// continue processing as if body was pure string
				dslBody = property.asString();
			}
			else if (!(inputMessage instanceof byte[])) {
				if (log.isDebugEnabled()) {
					log.debug(
							"Contract provided byte comparison, but the input message is of type ["
									+ inputMessage.getClass()
									+ "]. Can't compare the two.");
				}
				return null;
			}
			else {
				boolean matches = Arrays.equals(property.asBytes(),
						(byte[]) inputMessage);
				if (log.isDebugEnabled() && !matches) {
					log.debug(
							"Contract provided byte comparison, but the byte arrays don't match");
				}
				return matches ? groovyDsl : null;
			}
		}
		if (matchViaContent(groovyDsl, inputMessage, dslBody)) {
			return groovyDsl;
		}
		return null;
	}

	private boolean matchViaContent(Contract groovyDsl, Object inputMessage,
			Object dslBody) {
		boolean matches;
		ContentType type = ContentUtils.getClientContentType(inputMessage,
				groovyDsl.getInput().getMessageHeaders());
		if (type == ContentType.JSON) {
			BodyMatchers matchers = groovyDsl.getInput().getBodyMatchers();
			matches = matchesForJsonPayload(groovyDsl, inputMessage, matchers, dslBody);
		}
		else if (dslBody instanceof RegexProperty && inputMessage instanceof String) {
			Pattern pattern = ((RegexProperty) dslBody).getPattern();
			matches = pattern.matcher((String) inputMessage).matches();
			bodyUnmatchedLog(dslBody, matches, pattern);
		}
		else {
			matches = dslBody.equals(inputMessage);
			bodyUnmatchedLog(dslBody, matches, inputMessage);
		}
		return matches;
	}

	private void bodyUnmatchedLog(Object dslBody, boolean matches, Object pattern) {
		if (log.isDebugEnabled() && !matches) {
			log.debug("Body was supposed to " + unmatchedText(pattern)
					+ " but the value is [" + dslBody.toString() + "]");
		}
	}

	private boolean matchesForJsonPayload(Contract groovyDsl, Object inputMessage,
			BodyMatchers matchers, Object dslBody) {
		Object matchingInputMessage = JsonToJsonPathsConverter
				.removeMatchingJsonPaths(dslBody, matchers);
		JsonPaths jsonPaths = JsonToJsonPathsConverter
				.transformToJsonPathWithStubsSideValuesAndNoArraySizeCheck(
						matchingInputMessage);
		DocumentContext parsedJson;
		try {
			parsedJson = JsonPath
					.parse(this.objectMapper.writeValueAsString(inputMessage));
		}
		catch (JsonProcessingException e) {
			throw new IllegalStateException("Cannot serialize to JSON", e);
		}
		List<String> unmatchedJsonPath = new ArrayList<>();
		boolean matches = true;
		for (MethodBufferingJsonVerifiable path : jsonPaths) {
			matches &= matchesJsonPath(unmatchedJsonPath, parsedJson, path.jsonPath());
		}
		if (matchers != null && matchers.hasMatchers()) {
			for (BodyMatcher matcher : matchers.matchers()) {
				String jsonPath = JsonToJsonPathsConverter
						.convertJsonPathAndRegexToAJsonPath(matcher, dslBody);
				matches &= matchesJsonPath(unmatchedJsonPath, parsedJson, jsonPath);
			}
		}
		if (!unmatchedJsonPath.isEmpty()) {
			if (log.isDebugEnabled()) {
				log.debug("Contract [" + groovyDsl + "] didn't match the body due to "
						+ unmatchedJsonPath);
			}
		}
		return matches;
	}

	private boolean matchesJsonPath(List<String> unmatchedJsonPath,
			DocumentContext parsedJson, String jsonPath) {
		try {
			JsonAssertion.assertThat(parsedJson).matchesJsonPath(jsonPath);
			return true;
		}
		catch (Exception e) {
			unmatchedJsonPath.add(e.getLocalizedMessage());
			return false;
		}
	}

	private List<String> headersMatch(Message message, Contract groovyDsl) {
		List<String> unmatchedHeaders = new ArrayList<>();
		Map<String, Object> headers = message.getHeaders();
		for (Header it : groovyDsl.getInput().getMessageHeaders().getEntries()) {
			String name = it.getName();
			Object value = it.getClientValue();
			Object valueInHeader = headers.get(name);
			valueInHeader = valueInHeader instanceof byte[] ?
					fromByte((byte[]) valueInHeader) : valueInHeader;
			boolean matches;
			if (value instanceof RegexProperty) {
				Pattern pattern = ((RegexProperty) value).getPattern();
				matches = pattern.matcher(valueInHeader.toString()).matches();
			}
			else {
				matches = valueInHeader != null
						&& valueInHeader.toString().equals(value.toString());
			}
			if (!matches) {
				unmatchedHeaders.add("Header with name [" + name + "] was supposed to "
						+ unmatchedText(value) + " but the value is ["
						+ (valueInHeader != null ? valueInHeader.toString() : "null")
						+ "]");
			}
		}
		return unmatchedHeaders;
	}

	private String fromByte(byte[] valueInHeader) {
		String string = new String(valueInHeader);
		if (string.startsWith("\"") && string.endsWith("\"")) {
			return string.substring(1, string.length() - 1);
		}
		return string;
	}

	private String unmatchedText(Object expectedValue) {
		return expectedValue instanceof RegexProperty
				? "match pattern [" + ((RegexProperty) expectedValue).pattern() + "]"
				: "be equal to [" + expectedValue + "]";
	}

}
