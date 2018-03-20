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
import org.springframework.cloud.contract.spec.internal.Headers
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

	private static final Closure requestDslPropertyValueExtractor = { DslProperty property -> property.serverValue }
	private static final Closure responseDslPropertyValueExtractor = { DslProperty property -> property.clientValue }

	RequestResponsePact createFromContract(Contract contract) {
		assertNoExecutionProperty(contract)
		PactDslWithProvider pactDslWithProvider = ConsumerPactBuilder.consumer("Consumer")
				.hasPactWith("Provider")
		PactDslRequestWithPath pactDslRequest = createPactDslRequestWithPath(contract, pactDslWithProvider)
		PactDslResponse pactDslResponse = createPactDslResponse(contract, pactDslRequest)
		return pactDslResponse.toPact()
	}

	private void assertNoExecutionProperty(Contract contract) {
		assertNoExecutionPropertyInBody(contract.request.body, requestDslPropertyValueExtractor)
		assertNoExecutionPropertyInBody(contract.response.body, responseDslPropertyValueExtractor)
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
			traverseValues(dslPropertyValueExtractor(value), dslPropertyValueExtractor, closure)
		} else if (value instanceof Map) {
			value.values().forEach({traverseValues(it, dslPropertyValueExtractor, closure)})
		} else if (value instanceof Collection) {
			value.forEach({traverseValues(it, dslPropertyValueExtractor, closure)})
		} else {
			closure(value)
		}
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
			pactDslRequest = pactDslRequest.headers(headers(request.headers, requestDslPropertyValueExtractor))
		}
		if (request.body) {
			DslPart pactRequestBody = BodyConverter.toPactBody(request.body, requestDslPropertyValueExtractor)
			if (request.matchers) {
				pactRequestBody.setMatchers(MatchingRulesConverter.matchingRulesForBody(request.matchers))
			}
			pactDslRequest = pactDslRequest.body(pactRequestBody)
		}
		return pactDslRequest
	}

	private PactDslResponse createPactDslResponse(Contract contract, PactDslRequestWithPath pactDslRequest) {
		Response response = contract.response
		PactDslResponse pactDslResponse = pactDslRequest.willRespondWith()
				.status(response.status.clientValue as Integer)
		if (response.headers) {
			pactDslResponse = pactDslResponse.headers(headers(response.headers, responseDslPropertyValueExtractor))
		}
		if (response.body) {
			DslPart pactResponseBody = BodyConverter.toPactBody(response.body, responseDslPropertyValueExtractor)
			if (response.matchers) {
				pactResponseBody.setMatchers(MatchingRulesConverter.matchingRulesForBody(response.matchers))
			}
			pactDslResponse = pactDslResponse.body(pactResponseBody)
		}
		return pactDslResponse
	}

	private String url(Request request) {
		if (request.urlPath) {
			return request.urlPath.serverValue.toString()
		} else if (request.url) {
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
		} else if (request.url) {
			return request.url.queryParameters
		}
		throw new IllegalStateException("No url provided")

	}

	private Map<String, String> headers(Headers headers, Closure dslPropertyValueExtractor) {
		return headers.entries.collectEntries {
			String name = it.name
			String value = dslPropertyValueExtractor(it)
			return [(name) : value]
		}
	}
}