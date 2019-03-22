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

package org.springframework.cloud.contract.verifier.spec.pact

import au.com.dius.pact.consumer.ConsumerPactBuilder
import au.com.dius.pact.consumer.dsl.DslPart
import au.com.dius.pact.consumer.dsl.PactDslRequestWithPath
import au.com.dius.pact.consumer.dsl.PactDslResponse
import au.com.dius.pact.consumer.dsl.PactDslWithProvider
import au.com.dius.pact.model.RequestResponsePact
import groovy.transform.CompileStatic
import groovy.transform.PackageScope

import org.springframework.cloud.contract.spec.Contract
import org.springframework.cloud.contract.spec.internal.Body
import org.springframework.cloud.contract.spec.internal.DslProperty
import org.springframework.cloud.contract.spec.internal.ExecutionProperty
import org.springframework.cloud.contract.spec.internal.Header
import org.springframework.cloud.contract.spec.internal.QueryParameters
import org.springframework.cloud.contract.spec.internal.Request
import org.springframework.cloud.contract.spec.internal.Response

/**
 * Creator of {@link RequestResponsePact} instances
 *
 * @author Tim Ysewyn
 * @since 2.0.0
 */
@CompileStatic
@PackageScope
class RequestResponsePactCreator {

	RequestResponsePact createFromContract(List<Contract> contracts) {
		if (contracts.empty) {
			return null
		}
		Names names = NamingUtil.name(contracts.get(0))
		PactDslWithProvider pactDslWithProvider = ConsumerPactBuilder
				.consumer(names.consumer).hasPactWith(names.producer)
		PactDslResponse pactDslResponse = null
		contracts.each { Contract contract ->
			assertNoExecutionProperty(contract)
			PactDslRequestWithPath pactDslRequest = pactDslResponse ?
					createPactDslRequestWithPath(contract, pactDslResponse) :
					createPactDslRequestWithPath(contract, pactDslWithProvider)
			pactDslResponse = createPactDslResponse(contract, pactDslRequest)
		}
		return pactDslResponse.toPact()
	}

	private void assertNoExecutionProperty(Contract contract) {
		assertNoExecutionPropertyInBody(contract.request.body,
				{ DslProperty dslProperty -> dslProperty.serverValue })
		assertNoExecutionPropertyInBody(contract.response.body,
				{ DslProperty dslProperty -> dslProperty.clientValue })
	}

	private void assertNoExecutionPropertyInBody(Body body, Closure dslPropertyValueExtractor) {
		traverseValues(body, dslPropertyValueExtractor, {
			if (it instanceof ExecutionProperty) {
				throw new UnsupportedOperationException("We can't convert a contract that has execution property")
			}
		})
	}

	private void traverseValues(def value, Closure dslPropertyValueExtractor, Closure closure) {
		if (value instanceof DslProperty) {
			traverseValues(
					dslPropertyValueExtractor(value), dslPropertyValueExtractor, closure)
		}
		else if (value instanceof Map) {
			value.values().
					forEach({ traverseValues(it, dslPropertyValueExtractor, closure) })
		}
		else if (value instanceof Collection) {
			value.forEach({ traverseValues(it, dslPropertyValueExtractor, closure) })
		}
		else {
			closure(value)
		}
	}

	private PactDslRequestWithPath createPactDslRequestWithPath(Contract contract, PactDslResponse pactDslResponse) {
		Request request = contract.request
		PactDslRequestWithPath pactDslRequest = pactDslResponse
				.uponReceiving(contract.description ?: "")
				.path(url(request))
				.method(request.method.serverValue.toString())
		String query = query(request)
		if (query) {
			pactDslRequest = pactDslRequest.encodedQuery(query)
		}
		if (request.headers) {
			request.headers.entries.each { Header header ->
				pactDslRequest = processHeader(pactDslRequest, header)
			}
		}
		if (request.body) {
			DslPart pactRequestBody = BodyConverter.
					toPactBody(request.body, { DslProperty property -> property.serverValue })
			if (request.bodyMatchers) {
				pactRequestBody.setMatchers(MatchingRulesConverter.
						matchingRulesForBody(request.bodyMatchers))
			}
			pactRequestBody.setGenerators(ValueGeneratorConverter.
					extract(request.body, { DslProperty dslProperty -> dslProperty.clientValue }))
			pactDslRequest = pactDslRequest.body(pactRequestBody)
		}
		return pactDslRequest
	}

	private PactDslRequestWithPath createPactDslRequestWithPath(Contract contract, PactDslWithProvider pactDslWithProvider) {
		Request request = contract.request
		PactDslRequestWithPath pactDslRequest = pactDslWithProvider
				.uponReceiving(contract.description ?: "")
				.path(url(request))
				.method(request.method.serverValue.toString())
		String query = query(request)
		if (query) {
			pactDslRequest = pactDslRequest.encodedQuery(query)
		}
		if (request.headers) {
			request.headers.entries.each { Header header ->
				pactDslRequest = processHeader(pactDslRequest, header)
			}
		}
		if (request.body) {
			DslPart pactRequestBody = BodyConverter.
					toPactBody(request.body, { DslProperty property -> property.serverValue })
			if (request.bodyMatchers) {
				pactRequestBody.setMatchers(MatchingRulesConverter.
						matchingRulesForBody(request.bodyMatchers))
			}
			pactRequestBody.setGenerators(ValueGeneratorConverter.
					extract(request.body, { DslProperty dslProperty -> dslProperty.clientValue }))
			pactDslRequest = pactDslRequest.body(pactRequestBody)
		}
		return pactDslRequest
	}

	private PactDslResponse createPactDslResponse(Contract contract, PactDslRequestWithPath pactDslRequest) {
		Response response = contract.response
		PactDslResponse pactDslResponse = pactDslRequest.willRespondWith()
														.status(response.status.clientValue as Integer)
		if (response.headers) {
			response.headers.entries.each { Header header ->
				pactDslResponse = processHeader(pactDslResponse, header)
			}
		}
		if (response.body) {
			DslPart pactResponseBody = BodyConverter.
					toPactBody(response.body, { DslProperty property -> property.clientValue })
			if (response.bodyMatchers) {
				pactResponseBody.setMatchers(MatchingRulesConverter.
						matchingRulesForBody(response.bodyMatchers))
			}
			pactResponseBody.setGenerators(ValueGeneratorConverter.
					extract(response.body, { DslProperty dslProperty -> dslProperty.serverValue }))
			pactDslResponse = pactDslResponse.body(pactResponseBody)
		}
		return pactDslResponse
	}

	private String url(Request request) {
		if (request.urlPath) {
			return request.urlPath.serverValue.toString()
		}
		else if (request.url) {
			return request.url.serverValue.toString()
		}
		throw new IllegalStateException("No url provided")
	}

	private String query(Request request) {
		String query = null
		QueryParameters params = queryParams(request)
		if (params) {
			query = ""
			params.parameters.eachWithIndex { param, index ->
				query += param.name + '=' + param.serverValue
				if (index + 1 < params.parameters.size()) {
					query += '&'
				}
			}
		}
		return query
	}

	private QueryParameters queryParams(Request request) {
		if (request.urlPath) {
			return request.urlPath.queryParameters
		}
		else if (request.url) {
			return request.url.queryParameters
		}
		throw new IllegalStateException("No url provided")

	}

	private PactDslRequestWithPath processHeader(PactDslRequestWithPath pactDslRequest, Header header) {
		if (header.isSingleValue()) {
			String value = getDslPropertyServerValue(header).toString()
			return pactDslRequest.headers(header.name, value)
		}
		else {
			String regex = getDslPropertyClientValue(header).toString()
			String example = getDslPropertyServerValue(header).toString()
			return pactDslRequest.matchHeader(header.name, regex, example)
		}
	}

	private PactDslResponse processHeader(PactDslResponse pactDslResponse, Header header) {
		if (header.isSingleValue()) {
			String value = getDslPropertyClientValue(header).toString()
			return pactDslResponse.headers([(header.name): value])
		}
		else {
			String regex = getDslPropertyServerValue(header).toString()
			String example = getDslPropertyClientValue(header).toString()
			return pactDslResponse.matchHeader(header.name, regex, example)
		}
	}

	private Object getDslPropertyClientValue(Object o) {
		Object value = o
		if (value instanceof DslProperty) {
			value = getDslPropertyClientValue(value.getClientValue())
		}
		return value
	}

	private Object getDslPropertyServerValue(Object o) {
		Object value = o
		if (value instanceof DslProperty) {
			value = getDslPropertyServerValue(value.getServerValue())
		}
		return value
	}
}
