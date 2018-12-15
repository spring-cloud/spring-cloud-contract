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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.toomuchcoding.jsonassert.JsonAssertion;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Predicate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.cloud.contract.spec.Contract;
import org.springframework.cloud.contract.spec.internal.BodyMatcher;
import org.springframework.cloud.contract.spec.internal.BodyMatchers;
import org.springframework.cloud.contract.spec.internal.Header;
import org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierObjectMapper;
import org.springframework.cloud.contract.verifier.util.JsonPaths;
import org.springframework.cloud.contract.verifier.util.JsonToJsonPathsConverter;
import org.springframework.cloud.contract.verifier.util.MapConverter;
import org.springframework.cloud.contract.verifier.util.MethodBufferingJsonVerifiable;

/**
 * Passes through a message that matches the one defined in the DSL
 *
 * @author Marcin Grzejszczak
 */
class StubRunnerCamelPredicate implements Predicate {

	private static final Log log = LogFactory.getLog(StubRunnerCamelPredicate.class);

	private final List<Contract> groovyDsls;
	private final ContractVerifierObjectMapper objectMapper = new ContractVerifierObjectMapper();

	public StubRunnerCamelPredicate(List<Contract> groovyDsls) {
		this.groovyDsls = groovyDsls;
	}

	@Override
	public boolean matches(Exchange exchange) {
		Contract contract = getContract(exchange.getMessage());
		if (log.isDebugEnabled()) {
			log.debug("For exchange [" + exchange + "] found contract [" + contract + "]");
		}
		if (contract == null) {
			return false;
		}
		exchange.getIn().setBody(new StubRunnerCamelPayload(contract));
		return true;
	}

	private Contract getContract(Message message) {
		for (Contract groovyDsl : this.groovyDsls) {
			Contract contract = matchContract(message, groovyDsl);
			if (contract != null) {
				return contract;
			}
		}
		return null;
	}

	private Contract matchContract(Message message, Contract groovyDsl) {
		List<String> unmatchedHeaders = headersMatch(message, groovyDsl);
		if (!unmatchedHeaders.isEmpty()) {
			if (log.isDebugEnabled()) {
				log.debug("Contract [" + groovyDsl
						+ "] hasn't matched the following headers " + unmatchedHeaders);
			}
			return null;
		}
		Object inputMessage = message.getBody();
		BodyMatchers matchers = groovyDsl.getInput().getBodyMatchers();
		Object dslBody = MapConverter.getStubSideValues(groovyDsl.getInput().getMessageBody());
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
			for (BodyMatcher matcher : matchers.jsonPathMatchers()) {
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
		if (matches) {
			return groovyDsl;
		}
		return null;
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
			boolean matches;
			if (value instanceof Pattern) {
				Pattern pattern = (Pattern) value;
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

	private String unmatchedText(Object expectedValue) {
		return expectedValue instanceof Pattern
				? "match pattern [" + ((Pattern) expectedValue).pattern() + "]"
				: "be equal to [" + expectedValue + "]";
	}

}
