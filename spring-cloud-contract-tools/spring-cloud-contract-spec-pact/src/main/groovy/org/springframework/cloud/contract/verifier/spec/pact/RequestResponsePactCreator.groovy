package org.springframework.cloud.contract.verifier.spec.pact

import au.com.dius.pact.consumer.ConsumerPactBuilder
import au.com.dius.pact.consumer.dsl.DslPart
import au.com.dius.pact.consumer.dsl.PactDslRequestWithPath
import au.com.dius.pact.consumer.dsl.PactDslResponse
import au.com.dius.pact.consumer.dsl.PactDslWithProvider
import au.com.dius.pact.model.RequestResponsePact
import au.com.dius.pact.model.matchingrules.Category
import au.com.dius.pact.model.matchingrules.EqualsMatcher
import au.com.dius.pact.model.matchingrules.MaxTypeMatcher
import au.com.dius.pact.model.matchingrules.MinMaxTypeMatcher
import au.com.dius.pact.model.matchingrules.MinTypeMatcher
import au.com.dius.pact.model.matchingrules.RegexMatcher
import au.com.dius.pact.model.matchingrules.TypeMatcher
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import org.springframework.cloud.contract.spec.Contract
import org.springframework.cloud.contract.spec.internal.Body
import org.springframework.cloud.contract.spec.internal.BodyMatcher
import org.springframework.cloud.contract.spec.internal.BodyMatchers
import org.springframework.cloud.contract.spec.internal.DslProperty
import org.springframework.cloud.contract.spec.internal.ExecutionProperty
import org.springframework.cloud.contract.spec.internal.Headers
import org.springframework.cloud.contract.spec.internal.MatchingType
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

	static final Closure requestDslPropertyValueExtractor = { DslProperty property -> property.serverValue }
	static final Closure responseDslPropertyValueExtractor = { DslProperty property -> property.clientValue }

	RequestResponsePact createFromContract(Contract contract) {
		assertNoExecutionProperty(contract)

		PactDslWithProvider pactDslWithProvider = ConsumerPactBuilder.consumer("Consumer")
				.hasPactWith("Provider")

		PactDslRequestWithPath pactDslRequest = createPactDslRequestWithPath(contract, pactDslWithProvider)

		PactDslResponse pactDslResponse = createPactDslResponse(contract, pactDslRequest)

		pactDslResponse.toPact()
	}

	protected void assertNoExecutionProperty(Contract contract) {
		assertNoExecutionPropertyInBody(contract.request.body, requestDslPropertyValueExtractor)
		assertNoExecutionPropertyInBody(contract.response.body, responseDslPropertyValueExtractor)
	}

	protected void assertNoExecutionPropertyInBody(Body body, Closure dslPropertyValueExtractor) {
		traverseValues(body, dslPropertyValueExtractor, {
			if (it instanceof ExecutionProperty) {
				throw new UnsupportedOperationException("We can't convert a contract that has execution property")
			}
		})
	}

	protected void traverseValues(def value, Closure dslPropertyValueExtractor, Closure closure) {
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

		DslPart pactRequestBody = BodyConverter.toPactBody(request.body, requestDslPropertyValueExtractor)

		if (request.matchers) {
			pactRequestBody.setMatchers(matchingRulesForBody(request.matchers))
		}

		pactDslRequest.body(pactRequestBody)
	}

	private PactDslResponse createPactDslResponse(Contract contract, PactDslRequestWithPath pactDslRequest) {
		Response response = contract.response

		PactDslResponse pactDslResponse = pactDslRequest.willRespondWith()
				.status(response.status.clientValue as Integer)

		if (response.headers) {
			pactDslResponse = pactDslResponse.headers(headers(response.headers, responseDslPropertyValueExtractor))
		}

		DslPart pactResponseBody = BodyConverter.toPactBody(response.body, responseDslPropertyValueExtractor)


		if (response.matchers) {
			pactResponseBody.setMatchers(matchingRulesForBody(response.matchers))
		}

		pactDslResponse.body(pactResponseBody)
	}

	protected String url(Request request) {
		if (request.urlPath) {
			return request.urlPath.serverValue.toString()
		} else if (request.url) {
			return request.url.serverValue.toString()
		}
		throw new IllegalStateException("No url provided")
	}

	protected String query(Request request) {
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
		query
	}

	protected QueryParameters queryParams(Request request) {
		if (request.urlPath) {
			return request.urlPath.queryParameters
		} else if (request.url) {
			return request.url.queryParameters
		}
		throw new IllegalStateException("No url provided")

	}

	protected Map<String, String> headers(Headers headers, Closure dslPropertyValueExtractor) {
		return headers.entries.collectEntries {
			String name = it.name
			String value = dslPropertyValueExtractor(it)
			return [(name) : value]
		}
	}

	protected Category matchingRulesForBody(BodyMatchers bodyMatchers) {
		Category category = new Category("body")

		bodyMatchers.jsonPathMatchers().forEach({ BodyMatcher it ->
			String key = getMatcherKey(it.path())
			MatchingType matchingType = it.matchingType()

			switch (matchingType) {
				case MatchingType.EQUALITY:
					category.setRule(key, EqualsMatcher.INSTANCE)
					break
				case MatchingType.TYPE:
					if (it.minTypeOccurrence() && it.maxTypeOccurrence()) {
						category.setRule(key, new MinMaxTypeMatcher(it.minTypeOccurrence(), it.maxTypeOccurrence()))
					} else if (it.minTypeOccurrence()) {
						category.setRule(key, new MinTypeMatcher(it.minTypeOccurrence()))
					} else if (it.maxTypeOccurrence()) {
						category.setRule(key, new MaxTypeMatcher(it.maxTypeOccurrence()))
					} else {
						category.setRule(key, TypeMatcher.INSTANCE)
					}
					break
				case MatchingType.DATE:
				case MatchingType.TIME:
				case MatchingType.TIMESTAMP:
				case MatchingType.REGEX:
					category.setRule(key, new RegexMatcher(it.value().toString()))
					break
				default:
					break
			}
		})

		category
	}

	private String getMatcherKey(String path) {
		"${path.startsWith('$') ? path.substring(1) : path}"
	}

}