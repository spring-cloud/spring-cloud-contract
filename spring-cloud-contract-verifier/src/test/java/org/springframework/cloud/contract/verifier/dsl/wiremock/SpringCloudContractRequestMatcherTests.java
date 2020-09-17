/*
 * Copyright 2020-2020 the original author or authors.
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

package org.springframework.cloud.contract.verifier.dsl.wiremock;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.matching.MatchResult;
import org.assertj.core.api.BDDAssertions;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import org.springframework.cloud.contract.verifier.converter.YamlContract;

class SpringCloudContractRequestMatcherTests {

	@Test
	void should_not_match_when_contract_missing() {
		SpringCloudContractRequestMatcher matcher = new SpringCloudContractRequestMatcher() {

			@Override
			List<RequestMatcher> matchers() {
				return Collections.singletonList((contracts, request, parameters) -> {
					throw new UnsupportedOperationException("This should not be called");
				});
			}
		};

		MatchResult result = matcher.match(BDDMockito.mock(Request.class), Parameters.one("tool", "foo"));

		BDDAssertions.then(result.isExactMatch()).isFalse();
	}

	@Test
	void should_not_match_when_tool_missing() {
		SpringCloudContractRequestMatcher matcher = new SpringCloudContractRequestMatcher() {

			@Override
			List<RequestMatcher> matchers() {
				return Collections.singletonList((contracts, request, parameters) -> {
					throw new UnsupportedOperationException("This should not be called");
				});
			}
		};

		MatchResult result = matcher.match(BDDMockito.mock(Request.class), Parameters.one("contract", "value"));

		BDDAssertions.then(result.isExactMatch()).isFalse();
	}

	@Test
	void should_not_match_when_unsupported_tool() {
		SpringCloudContractRequestMatcher matcher = new SpringCloudContractRequestMatcher() {

			@Override
			List<RequestMatcher> matchers() {
				return Collections.singletonList((contracts, request, parameters) -> {
					throw new UnsupportedOperationException("This should not be called");
				});
			}
		};

		MatchResult result = matcher.match(BDDMockito.mock(Request.class), Parameters.one("contract", "value"));

		BDDAssertions.then(result.isExactMatch()).isFalse();
	}

	private static final String PROPER_YAML = "---\n" + "request:\n" + "  method: \"POST\"\n" + "  url: \"/graphql\"\n"
			+ "  headers:\n" + "    Content-Type: \"application/json\"\n" + "  body:\n"
			+ "    query: \"query queryName($personName: String!) { personToCheck(name: $personName)"
			+ "      {         name    age  } }\"\n" + "    variables:\n" + "      personName: \"Old Enough\"\n"
			+ "    operationName: \"queryName\"\n" + "  matchers:\n" + "    headers:\n"
			+ "      - key: \"Content-Type\"\n" + "        regex: \"application/json.*\"\n"
			+ "        regexType: \"as_string\"\n" + "response:\n" + "  status: 200\n" + "  headers:\n"
			+ "    Content-Type: \"application/json\"\n" + "  body:\n" + "    data:\n" + "      personToCheck:\n"
			+ "        name: \"Old Enough\"\n" + "        age: \"40\"\n" + "  matchers:\n" + "    headers:\n"
			+ "      - key: \"Content-Type\"\n" + "        regex: \"application/json.*\"\n"
			+ "        regexType: \"as_string\"\n" + "name: \"shouldRetrieveOldEnoughPerson\"\n" + "metadata:\n"
			+ "  verifier:\n" + "    tool: \"graphql\"\n";

	@Test
	void should_not_match_when_exception_occurs_while_trying_to_parse_contract() {
		SpringCloudContractRequestMatcher matcher = new SpringCloudContractRequestMatcher() {
			@Override
			List<RequestMatcher> matchers() {
				return Collections.singletonList((contracts, request, parameters) -> {
					throw new UnsupportedOperationException("This should not be called");
				});
			}
		};

		MatchResult result = matcher.match(BDDMockito.mock(Request.class),
				toMap(Tuples.of("tool", "unsupported"), Tuples.of("contract", PROPER_YAML)));

		BDDAssertions.then(result.isExactMatch()).isFalse();
	}
	// @formatter:off
	// @formatter:on

	@Test
	void should_delegate_to_an_applicable_request_matcher() {
		SpringCloudContractRequestMatcher matcher = new SpringCloudContractRequestMatcher() {
			@Override
			List<RequestMatcher> matchers() {
				return Collections.singletonList(new ApplicableRequestMatcher());
			}
		};

		MatchResult result = matcher.match(BDDMockito.mock(Request.class),
				toMap(Tuples.of("tool", "graphql"), Tuples.of("contract", PROPER_YAML)));

		BDDAssertions.then(result.isExactMatch()).isTrue();
	}

	private Parameters toMap(Tuple2<String, Object>... tuple2) {
		Map<String, Object> map = new HashMap<>();
		for (Tuple2<String, Object> tuple : tuple2) {
			map.put(tuple.getT1(), tuple.getT2());
		}
		return Parameters.from(map);
	}

}

class ApplicableRequestMatcher implements RequestMatcher {

	@Override
	public MatchResult match(List<YamlContract> contracts, Request request, Parameters parameters) {
		return MatchResult.of(true);
	}

	@Override
	public boolean isApplicable(String tool) {
		return true;
	}

}
