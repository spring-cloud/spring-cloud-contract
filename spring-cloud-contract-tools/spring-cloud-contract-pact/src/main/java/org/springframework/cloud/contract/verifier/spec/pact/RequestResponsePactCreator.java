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

package org.springframework.cloud.contract.verifier.spec.pact;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import au.com.dius.pact.consumer.ConsumerPactBuilder;
import au.com.dius.pact.consumer.dsl.DslPart;
import au.com.dius.pact.consumer.dsl.PactDslRequestWithPath;
import au.com.dius.pact.consumer.dsl.PactDslResponse;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.dsl.PactDslWithState;
import au.com.dius.pact.core.model.RequestResponsePact;
import org.apache.commons.lang3.StringUtils;

import org.springframework.cloud.contract.spec.Contract;
import org.springframework.cloud.contract.spec.internal.Body;
import org.springframework.cloud.contract.spec.internal.Cookies;
import org.springframework.cloud.contract.spec.internal.DslProperty;
import org.springframework.cloud.contract.spec.internal.ExecutionProperty;
import org.springframework.cloud.contract.spec.internal.Header;
import org.springframework.cloud.contract.spec.internal.QueryParameters;
import org.springframework.cloud.contract.spec.internal.RegexProperty;
import org.springframework.cloud.contract.spec.internal.Request;
import org.springframework.cloud.contract.spec.internal.Response;
import org.springframework.util.CollectionUtils;

import static java.util.stream.Collectors.joining;

/**
 * Creator of {@link RequestResponsePact} instances.
 *
 * @author Tim Ysewyn
 * @author Stessy Delcroix
 * @since 2.0.0
 */
class RequestResponsePactCreator {

	RequestResponsePact createFromContract(List<Contract> contracts) {
		if (CollectionUtils.isEmpty(contracts)) {
			return null;
		}
		Names names = NamingUtil.name(contracts.get(0));
		PactDslWithProvider pactDslWithProvider = ConsumerPactBuilder.consumer(names.getConsumer())
				.hasPactWith(names.getProducer());
		PactDslResponse pactDslResponse = null;
		for (Contract contract : contracts) {
			assertNoExecutionProperty(contract);
			PactDslRequestWithPath pactDslRequest = pactDslResponse != null
					? createPactDslRequestWithPath(contract, pactDslResponse)
					: createPactDslRequestWithPath(contract, pactDslWithProvider);
			pactDslResponse = createPactDslResponse(contract, pactDslRequest);
		}
		return pactDslResponse.toPact();
	}

	private void assertNoExecutionProperty(Contract contract) {
		assertNoExecutionPropertyInBody(contract.getRequest().getBody(), DslProperty::getServerValue);
		assertNoExecutionPropertyInBody(contract.getResponse().getBody(), DslProperty::getClientValue);
	}

	private void assertNoExecutionPropertyInBody(Body body,
			Function<DslProperty<?>, Object> dslPropertyValueExtractor) {
		traverseValues(body, dslPropertyValueExtractor, (Object object) -> {
			if (object instanceof ExecutionProperty) {
				throw new UnsupportedOperationException("We can't convert a contract that has execution property");
			}
			return object;
		});
	}

	private void traverseValues(Object value, Function<DslProperty<?>, Object> dslPropertyValueExtractor,
			Function<Object, Object> function) {
		if (value instanceof DslProperty) {
			traverseValues(dslPropertyValueExtractor.apply((DslProperty<?>) value), dslPropertyValueExtractor,
					function);
		}
		else if (value instanceof Map) {
			((Map) value).values().forEach(v -> traverseValues(v, dslPropertyValueExtractor, function));
		}
		else if (value instanceof Collection) {
			((Collection<?>) value).forEach(v -> traverseValues(v, dslPropertyValueExtractor, function));
		}
		else {
			function.apply(value);
		}
	}

	private PactDslRequestWithPath createPactDslRequestWithPath(Contract contract, PactDslResponse pactDslResponse) {
		PactDslRequestWithPath pactDslRequest = getPactDslRequest(contract,
				getPactDslWithStateFunction(pactDslResponse), getPactDslRequestWithPathBiFunction(pactDslResponse));
		Request request = contract.getRequest();
		final PactDslRequestWithPath finalPactDslRequest = pactDslRequest;
		if (request.getHeaders() != null) {
			request.getHeaders().getEntries().forEach(h -> processHeader(finalPactDslRequest, h));
		}
		if (request.getCookies() != null) {
			pactDslRequest = processCookies(finalPactDslRequest, request.getCookies());
		}
		if (request.getBody() != null) {
			DslPart pactRequestBody = BodyConverter.toPactBody(request.getBody(), DslProperty::getClientValue);
			if (request.getBodyMatchers() != null) {
				pactRequestBody.setMatchers(MatchingRulesConverter.matchingRulesForBody(request.getBodyMatchers()));
			}
			pactRequestBody
					.setGenerators(ValueGeneratorConverter.extract(request.getBody(), DslProperty::getClientValue));
			pactDslRequest = pactDslRequest.body(pactRequestBody);
		}
		return pactDslRequest;
	}

	private Function<PactMetaData.ProviderStateMetadata, PactDslWithState> getPactDslWithStateFunction(
			PactDslResponse pactDslResponse) {
		return stateMetadata -> pactDslResponse.given(stateMetadata.getName(), stateMetadata.getParams());
	}

	private BiFunction<String, Request, PactDslRequestWithPath> getPactDslRequestWithPathBiFunction(
			PactDslResponse pactDslResponse) {
		return (description, request) -> pactDslResponse.uponReceiving(description).path(url(request))
				.method(request.getMethod().getServerValue().toString());
	}

	private PactDslRequestWithPath createPactDslRequestWithPath(Contract contract,
			PactDslWithProvider pactDslWithProvider) {
		PactDslRequestWithPath pactDslRequest = getPactDslRequest(contract,
				getPactDslWithStateFunction(pactDslWithProvider),
				getPactDslRequestWithPathBiFunction(pactDslWithProvider));
		Request request = contract.getRequest();
		final PactDslRequestWithPath finalPactDslRequest = pactDslRequest;
		if (request.getHeaders() != null) {
			request.getHeaders().getEntries().forEach(h -> processHeader(finalPactDslRequest, h));
		}
		if (request.getBody() != null) {
			DslPart pactRequestBody = BodyConverter.toPactBody(request.getBody(), DslProperty::getServerValue);
			if (request.getBodyMatchers() != null) {
				pactRequestBody.setMatchers(MatchingRulesConverter.matchingRulesForBody(request.getBodyMatchers()));
			}
			pactRequestBody
					.setGenerators(ValueGeneratorConverter.extract(request.getBody(), DslProperty::getClientValue));
			pactDslRequest = pactDslRequest.body(pactRequestBody);
		}
		return pactDslRequest;
	}

	private Function<PactMetaData.ProviderStateMetadata, PactDslWithState> getPactDslWithStateFunction(
			PactDslWithProvider pactDslWithProvider) {
		return stateMetadata -> pactDslWithProvider.given(stateMetadata.getName(), stateMetadata.getParams());
	}

	private BiFunction<String, Request, PactDslRequestWithPath> getPactDslRequestWithPathBiFunction(
			PactDslWithProvider pactDslWithProvider) {
		return (description, request) -> pactDslWithProvider.uponReceiving(description).path(url(request))
				.method(request.getMethod().getServerValue().toString());
	}

	private PactDslRequestWithPath getPactDslRequest(Contract contract,
			Function<PactMetaData.ProviderStateMetadata, PactDslWithState> pactDslWithStateFunction,
			BiFunction<String, Request, PactDslRequestWithPath> pactDslRequestWithPathBiFunction) {
		PactDslWithState pactDslWithState = getPactDslWithState(contract, pactDslWithStateFunction);
		String description = StringUtils.isNotBlank(contract.getDescription()) ? contract.getDescription() : "";
		Request request = contract.getRequest();
		PactDslRequestWithPath pactDslRequest;
		if (pactDslWithState != null) {
			pactDslRequest = pactDslWithState.uponReceiving(description).path(url(request))
					.method(request.getMethod().getServerValue().toString());
		}
		else {
			pactDslRequest = pactDslRequestWithPathBiFunction.apply(description, request);
		}
		String query = query(request);
		if (StringUtils.isNotBlank(query)) {
			pactDslRequest = pactDslRequest.encodedQuery(query);
		}
		return pactDslRequest;
	}

	private PactDslWithState getPactDslWithState(Contract contract,
			Function<PactMetaData.ProviderStateMetadata, PactDslWithState> pactDslWithStateFunction) {
		PactDslWithState pactDslWithState = null;
		if (contract.getMetadata().containsKey(PactMetaData.METADATA_KEY)) {
			PactMetaData metadata = PactMetaData.fromMetadata(contract.getMetadata());
			if (!metadata.getProviderStates().isEmpty()) {
				for (PactMetaData.ProviderStateMetadata stateMetadata : metadata.getProviderStates()) {
					if (pactDslWithState == null) {
						pactDslWithState = pactDslWithStateFunction.apply(stateMetadata);
					}
					else {
						pactDslWithState = pactDslWithState.given(stateMetadata.getName(), stateMetadata.getParams());
					}
				}
			}
		}
		return pactDslWithState;
	}

	private String url(Request request) {
		if (request.getUrlPath() != null) {
			return request.getUrlPath().getServerValue().toString();
		}
		else if (request.getUrl() != null) {
			return request.getUrl().getServerValue().toString();
		}
		throw new IllegalStateException("No url provided");
	}

	private String query(Request request) {
		final StringBuilder query = new StringBuilder();
		QueryParameters params = queryParams(request);
		if (params != null) {
			AtomicInteger index = new AtomicInteger();
			params.getParameters().forEach(p -> {
				query.append(p.getName()).append('=').append(p.getServerValue());
				if (index.incrementAndGet() < params.getParameters().size()) {
					query.append('&');
				}

			});
		}
		return query.toString();
	}

	private QueryParameters queryParams(Request request) {
		if (request.getUrlPath() != null) {
			return request.getUrlPath().getQueryParameters();
		}
		else if (request.getUrl() != null) {
			return request.getUrl().getQueryParameters();
		}
		throw new IllegalStateException("No url provided");

	}

	private PactDslRequestWithPath processHeader(PactDslRequestWithPath pactDslRequest, Header header) {
		if (header.isSingleValue()) {
			String value = getDslPropertyServerValue(header).toString();
			return pactDslRequest.headers(header.getName(), value);
		}
		else {
			String regex = getDslPropertyClientValue(header).toString();
			String example = getDslPropertyServerValue(header).toString();
			return pactDslRequest.matchHeader(header.getName(), regex, example);
		}
	}

	private String stubSideCookieExample(Cookies cookies) {
		return cookies.asStubSideMap().entrySet().stream().map(Object::toString).collect(joining(";"));
	}

	private Object getDslPropertyClientValue(Object o) {
		Object value = o;
		if (value instanceof DslProperty) {
			value = getDslPropertyClientValue(((DslProperty) value).getClientValue());
		}
		return value;
	}

	private Object getDslPropertyServerValue(Object o) {
		Object value = o;
		if (value instanceof DslProperty) {
			value = getDslPropertyServerValue(((DslProperty) value).getServerValue());
		}
		return value;
	}

	private PactDslRequestWithPath processCookies(PactDslRequestWithPath pactDslRequest, Cookies cookies) {
		Map<String, Object> stubSideCookies = cookies.asStubSideMap();
		Collection<RegexProperty> regexProperties = stubSideCookies.values().stream()
				.filter(r -> r instanceof Pattern || r instanceof RegexProperty).map(RegexProperty::new)
				.collect(Collectors.toList());
		if (!regexProperties.isEmpty()) {
			String regex = regexProperties.stream().map(RegexProperty::pattern).collect(joining("|"));
			return pactDslRequest.matchHeader("Cookie", regex, testSideCookieExample(cookies));
		}
		else {
			return pactDslRequest.headers("Cookie", testSideCookieExample(cookies));
		}
	}

	private String testSideCookieExample(Cookies cookies) {
		return cookies.asTestSideMap().entrySet().stream().map(Object::toString).collect(joining(";"));
	}

	private PactDslResponse createPactDslResponse(Contract contract, PactDslRequestWithPath pactDslRequest) {
		Response response = contract.getResponse();
		PactDslResponse pactDslResponse = pactDslRequest.willRespondWith()
				.status((Integer) response.getStatus().getClientValue());

		PactDslResponse finalPactDslResponse = pactDslResponse;
		if (response.getHeaders() != null) {
			response.getHeaders().getEntries().forEach(h -> processHeader(finalPactDslResponse, h));
		}

		if (response.getCookies() != null) {
			pactDslResponse = processCookies(pactDslResponse, response.getCookies());
		}
		if (response.getBody() != null) {
			DslPart pactResponseBody = BodyConverter.toPactBody(response.getBody(), DslProperty::getClientValue);
			if (response.getBodyMatchers() != null) {
				pactResponseBody.setMatchers(MatchingRulesConverter.matchingRulesForBody(response.getBodyMatchers()));
			}
			pactResponseBody
					.setGenerators(ValueGeneratorConverter.extract(response.getBody(), DslProperty::getServerValue));
			pactDslResponse = pactDslResponse.body(pactResponseBody);
		}
		return pactDslResponse;
	}

	private PactDslResponse processHeader(PactDslResponse pactDslResponse, Header header) {
		if (header.isSingleValue()) {
			String value = getDslPropertyClientValue(header).toString();
			return pactDslResponse.headers(Collections.singletonMap(header.getName(), value));
		}
		else {
			String regex = getDslPropertyServerValue(header).toString();
			String example = getDslPropertyClientValue(header).toString();
			return pactDslResponse.matchHeader(header.getName(), regex, example);
		}
	}

	private PactDslResponse processCookies(PactDslResponse pactDslResponse, Cookies cookies) {
		Map<String, Object> testSideCookies = cookies.asTestSideMap();
		Collection<RegexProperty> regexProperties = testSideCookies.values().stream()
				.filter(p -> p instanceof Pattern || p instanceof RegexProperty).map(RegexProperty::new)
				.collect(Collectors.toList());
		if (!regexProperties.isEmpty()) {
			String regex = regexProperties.stream().map(RegexProperty::pattern).collect(joining("|"));
			return pactDslResponse.matchHeader("Cookie", regex, stubSideCookieExample(cookies));
		}
		else {
			return pactDslResponse.headers(Collections.singletonMap("Cookie", stubSideCookieExample(cookies)));
		}
	}

}
