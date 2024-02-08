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

import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.matching.MatchResult;
import org.assertj.core.api.BDDAssertions;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;

import org.springframework.cloud.contract.verifier.converter.YamlContractConverter;

class GraphQLRequestMatcherTests {

	// @formatter:off
	private static final String YAML_WITH_INVALID_VARIABLES = "---\n"
			+ "request:\n"
			+ "  method: \"POST\"\n"
			+ "  url: \"/graphql\"\n"
			+ "  headers:\n"
			+ "    Content-Type: \"application/json\"\n"
			+ "  body:\n"
			+ "    query: \"query queryName($personName: String!) {\\n  personToCheck(name: $personName)\\\n"
			+ "      \\ {\\n    name\\n    age\\n  }\\n}\\n\\n\\n\\n\"\n"
			+ "    variables: This should actually be a map not a string\n"
			+ "    operationName: \"queryName\"\n"
			+ "  matchers:\n"
			+ "    headers:\n"
			+ "      - key: \"Content-Type\"\n"
			+ "        regex: \"application/json.*\"\n"
			+ "        regexType: \"as_string\"\n"
			+ "response:\n"
			+ "  status: 200\n"
			+ "  headers:\n"
			+ "    Content-Type: \"application/json\"\n"
			+ "  body:\n"
			+ "    data:\n"
			+ "      personToCheck:\n"
			+ "        name: \"Old Enough\"\n"
			+ "        age: \"40\"\n"
			+ "  matchers:\n"
			+ "    headers:\n"
			+ "      - key: \"Content-Type\"\n"
			+ "        regex: \"application/json.*\"\n"
			+ "        regexType: \"as_string\"\n"
			+ "name: \"shouldRetrieveOldEnoughPerson\"\n"
			+ "metadata:\n"
			+ "  verifier:\n"
			+ "    tool: \"graphql\"\n";
	// @formatter:on

	@Test
	void should_not_match_when_exception_occurs_while_trying_to_read_missing_request_body() {
		GraphQlMatcher matcher = new GraphQlMatcher();

		MatchResult result = matcher.match(YamlContractConverter.INSTANCE.read(YAML_WITH_INVALID_VARIABLES.getBytes()),
				BDDMockito.mock(Request.class), null);

		BDDAssertions.then(result.isExactMatch()).isFalse();
	}

	@Test
	void should_not_match_when_exception_occurs_while_trying_to_parse_graphql_entries() {
		GraphQlMatcher matcher = new GraphQlMatcher();

		MatchResult result = matcher.match(YamlContractConverter.INSTANCE.read(YAML_WITH_INVALID_VARIABLES.getBytes()),
				request(), null);

		BDDAssertions.then(result.isExactMatch()).isFalse();
	}

	// @formatter:off
	private static final String PROPER_YAML = "---\n"
			+ "request:\n"
			+ "  method: \"POST\"\n"
			+ "  url: \"/graphql\"\n"
			+ "  headers:\n"
			+ "    Content-Type: \"application/json\"\n"
			+ "  body:\n"
			+ "    query: \"query queryName($personName: String!) { personToCheck(name: $personName)"
			+ "      {         name    age  } }\"\n"
			+ "    variables:\n"
			+ "      personName: \"Old Enough\"\n"
			+ "    operationName: \"queryName\"\n"
			+ "  matchers:\n"
			+ "    headers:\n"
			+ "      - key: \"Content-Type\"\n"
			+ "        regex: \"application/json.*\"\n"
			+ "        regexType: \"as_string\"\n"
			+ "response:\n"
			+ "  status: 200\n"
			+ "  headers:\n"
			+ "    Content-Type: \"application/json\"\n"
			+ "  body:\n"
			+ "    data:\n"
			+ "      personToCheck:\n"
			+ "        name: \"Old Enough\"\n"
			+ "        age: \"40\"\n"
			+ "  matchers:\n"
			+ "    headers:\n"
			+ "      - key: \"Content-Type\"\n"
			+ "        regex: \"application/json.*\"\n"
			+ "        regexType: \"as_string\"\n"
			+ "name: \"shouldRetrieveOldEnoughPerson\"\n"
			+ "metadata:\n"
			+ "  verifier:\n"
			+ "    tool: \"graphql\"\n";
	// @formatter:on

	@Test
	void should_not_match_when_unsupported_tool() {
		BDDAssertions.then(new GraphQlMatcher().isApplicable("unknown")).isFalse();
	}

	@Test
	void should_match_when_the_graphql_part_matches_regardless_of_whitespace_entries_in_the_query() {
		GraphQlMatcher matcher = new GraphQlMatcher();

		MatchResult result = matcher.match(YamlContractConverter.INSTANCE.read(PROPER_YAML.getBytes()), request(),
				null);

		BDDAssertions.then(result.isExactMatch()).isTrue();
	}

	// @formatter:off
	private static final String NOT_MATCHING_QUERY_BODY = "{\n"
			+ "\"query\":\"this should not match\",\n"
			+ "\"variables\":{\"personName\":\"Old Enough\"},\n"
			+ "\"operationName\":\"queryName\"\n"
			+ "}";
	// @formatter:on

	@Test
	void should_not_match_when_the_query_does_not_match() {
		GraphQlMatcher matcher = new GraphQlMatcher();

		MatchResult result = matcher.match(YamlContractConverter.INSTANCE.read(PROPER_YAML.getBytes()),
				request(NOT_MATCHING_QUERY_BODY), null);

		BDDAssertions.then(result.isExactMatch()).isFalse();
	}

	// @formatter:off
	private static final String NOT_MATCHING_VARIABLES_BODY = "{\n"
			+ "\"query\":\"query queryName($personName: String!) {\\n  personToCheck(name: $personName) {\\n    name\\n    age\\n  }\\n}\\n\\n\\n\\n\",\n"
			+ "\"variables\":{\"Not matching key\":\"Not matching value\"},\n"
			+ "\"operationName\":\"queryName\"\n"
			+ "}";
	// @formatter:on

	@Test
	void should_not_match_when_the_variables_does_not_match() {
		GraphQlMatcher matcher = new GraphQlMatcher();

		MatchResult result = matcher.match(YamlContractConverter.INSTANCE.read(PROPER_YAML.getBytes()),
				request(NOT_MATCHING_VARIABLES_BODY), null);

		BDDAssertions.then(result.isExactMatch()).isFalse();
	}

	// @formatter:off
	private static final String NOT_MATCHING_OPERATION_NAME_BODY = "{\n"
			+ "\"query\":\"query queryName($personName: String!) {\\n  personToCheck(name: $personName) {\\n    name\\n    age\\n  }\\n}\\n\\n\\n\\n\",\n"
			+ "\"variables\":{\"personName\":\"Old Enough\"},\n"
			+ "\"operationName\":\"not matching operation name\"\n"
			+ "}";
	// @formatter:on

	@Test
	void should_not_match_when_the_operation_name_does_not_match() {
		GraphQlMatcher matcher = new GraphQlMatcher();

		MatchResult result = matcher.match(YamlContractConverter.INSTANCE.read(PROPER_YAML.getBytes()),
				request(NOT_MATCHING_OPERATION_NAME_BODY), null);

		BDDAssertions.then(result.isExactMatch()).isFalse();
	}

	// @formatter:off
	private static final String REQUEST_BODY = "{\n"
			+ "\"query\":\"query queryName($personName: String!) {\\n  personToCheck(name: $personName) {\\n    name\\n    age\\n  }\\n}\\n\\n\\n\\n\",\n"
			+ "\"variables\":{\"personName\":\"Old Enough\"},\n"
			+ "\"operationName\":\"queryName\"\n"
			+ "}";
	// @formatter:on

	private Request request() {
		return request(REQUEST_BODY);
	}

	private Request request(String body) {
		Request request = BDDMockito.mock(Request.class);
		BDDMockito.given(request.getBody()).willReturn(body.getBytes());
		return request;
	}

}
