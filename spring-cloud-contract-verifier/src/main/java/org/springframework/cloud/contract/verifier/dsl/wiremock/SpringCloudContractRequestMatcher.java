/*
 * Copyright 2013-2020 the original author or authors.
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

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.matching.MatchResult;
import com.github.tomakehurst.wiremock.matching.RequestMatcherExtension;
import net.javacrumbs.jsonunit.assertj.JsonAssertions;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.assertj.core.api.Assertions;

import org.springframework.cloud.contract.verifier.converter.YamlContract;
import org.springframework.cloud.contract.verifier.converter.YamlContractConverter;

/**
 * Provides custom matching for WireMock's stub requests.
 *
 * @author Marcin Grzejszczak
 * @since 3.0.0
 */
public class SpringCloudContractRequestMatcher extends RequestMatcherExtension {

	private static final List<String> SUPPORTED_TOOLS = Arrays.asList(GraphQlMatcher.NAME);

	/**
	 * Name of the transformer inside the stub.
	 */
	public static final String NAME = "spring-cloud-contract";

	private static final Log log = LogFactory.getLog(SpringCloudContractRequestMatcher.class);

	@Override
	public MatchResult match(Request request, Parameters parameters) {
		if (!parameters.containsKey("contract") || !parameters.containsKey("tool")) {
			return MatchResult.noMatch();
		}
		String tool = parameters.getString("tool");
		if (!SUPPORTED_TOOLS.contains(tool)) {
			if (log.isWarnEnabled()) {
				log.warn("The tool [" + tool + "] is not supported");
			}
			return MatchResult.noMatch();
		}
		String string = parameters.getString("contract");
		List<YamlContract> contracts;
		try {
			contracts = YamlContractConverter.INSTANCE.read(string.getBytes());
		}
		catch (Exception e) {
			if (log.isWarnEnabled()) {
				log.warn("An exception occurred while trying to parse the contract", e);
			}
			return MatchResult.noMatch();
		}
		return new RequestMatcherFactory(matchers()).pick(tool).match(contracts, request, parameters);
	}

	List<RequestMatcher> matchers() {
		return Arrays.asList(new GraphQlMatcher());
	}

	@Override
	public String getName() {
		return NAME;
	}

}

class RequestMatcherFactory {

	private final List<RequestMatcher> matchers;

	RequestMatcherFactory(List<RequestMatcher> matchers) {
		this.matchers = matchers;
	}

	RequestMatcher pick(String tool) {
		return this.matchers.stream().filter(m -> m.isApplicable(tool)).findFirst()
				.orElse(new NotMatchingRequestMatcher());
	}

}

interface RequestMatcher {

	MatchResult match(List<YamlContract> contracts, Request request, Parameters parameters);

	default boolean assertThat(Runnable runnable) {
		try {
			runnable.run();
			return true;
		}
		catch (Exception | AssertionError er) {
			return false;
		}
	}

	default boolean isApplicable(String tool) {
		return false;
	}

}

class NotMatchingRequestMatcher implements RequestMatcher {

	@Override
	public MatchResult match(List<YamlContract> contracts, Request request, Parameters parameters) {
		return MatchResult.noMatch();
	}

	@Override
	public boolean isApplicable(String tool) {
		return true;
	}

}

class GraphQlMatcher implements RequestMatcher {

	static final String NAME = "graphql";

	private static final Log log = LogFactory.getLog(GraphQlMatcher.class);

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Override
	public MatchResult match(List<YamlContract> contracts, Request request, Parameters parameters) {
		YamlContract contract = contracts.get(0);
		// TODO: What if the body is in files?
		Map body = (Map) contract.request.body;
		try {
			Map jsonBodyFromContract = body;
			Map jsonBodyFromRequest = this.objectMapper.readerForMapOf(Object.class).readValue(request.getBody());
			String query = (String) jsonBodyFromContract.get("query");
			String queryFromRequest = (String) jsonBodyFromRequest.get("query");
			Map variables = (Map) jsonBodyFromContract.get("variables");
			Map variablesFromRequest = (Map) jsonBodyFromRequest.get("variables");
			String operationName = (String) jsonBodyFromContract.get("operationName");
			String operationNameFromRequest = (String) jsonBodyFromRequest.get("operationName");
			boolean queryMatches = assertThat(
					() -> Assertions.assertThat(query).isEqualToIgnoringWhitespace(queryFromRequest));
			boolean variablesMatch = assertThat(
					() -> JsonAssertions.assertThatJson(variables).isEqualTo(variablesFromRequest));
			boolean operationMatches = StringUtils.equals(operationName, operationNameFromRequest);
			return MatchResult.of(queryMatches && variablesMatch && operationMatches);
		}
		catch (Exception e) {
			if (log.isWarnEnabled()) {
				log.warn("An exception occurred while trying to parse the graphql entries", e);
			}
			return MatchResult.noMatch();
		}
	}

	@Override
	public boolean isApplicable(String tool) {
		return NAME.equals(tool);
	}

}
