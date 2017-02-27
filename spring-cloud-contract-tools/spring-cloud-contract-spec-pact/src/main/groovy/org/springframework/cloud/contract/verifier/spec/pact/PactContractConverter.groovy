package org.springframework.cloud.contract.verifier.spec.pact

import au.com.dius.pact.model.BasePact
import au.com.dius.pact.model.Consumer
import au.com.dius.pact.model.Interaction
import au.com.dius.pact.model.OptionalBody
import au.com.dius.pact.model.Pact
import au.com.dius.pact.model.PactReader
import au.com.dius.pact.model.Provider
import au.com.dius.pact.model.Request
import au.com.dius.pact.model.RequestResponseInteraction
import au.com.dius.pact.model.RequestResponsePact
import au.com.dius.pact.model.Response
import groovy.json.JsonOutput
import groovy.transform.CompileStatic
import org.springframework.cloud.contract.spec.Contract
import org.springframework.cloud.contract.spec.ContractConverter
import org.springframework.cloud.contract.spec.internal.BodyMatchers
import org.springframework.cloud.contract.spec.internal.DslProperty
import org.springframework.cloud.contract.spec.internal.ExecutionProperty
import org.springframework.cloud.contract.spec.internal.Headers
import org.springframework.cloud.contract.spec.internal.MatchingType
import org.springframework.cloud.contract.spec.internal.QueryParameters
import org.springframework.cloud.contract.verifier.util.MapConverter

/**
 * Converter of JSON PACT file
 *
 * @author Marcin Grzejszczak
 * @since 1.1.0
 */
@CompileStatic
class PactContractConverter implements ContractConverter<Pact> {

	private static final String MATCH_KEY = "match"
	private static final String REGEX_KEY = "regex"
	private static final String MAX_KEY = "max"
	private static final String MIN_KEY = "min"

	@Override
	boolean isAccepted(File file) {
		try {
			PactReader.loadPact(file)
			return true
		} catch (Exception e) {
			return false
		}
	}

	@Override
	Collection<Contract> convertFrom(File file) {
		Pact pact = PactReader.loadPact(file)
		List<Interaction> interactions = pact.interactions
		return interactions.collect { Interaction interaction ->
			Contract.make {
				if (interaction instanceof RequestResponseInteraction) {
					RequestResponseInteraction requestResponseInteraction = (RequestResponseInteraction) interaction
					description("$requestResponseInteraction.description${providerState(interaction)}")
					request {
						method(requestResponseInteraction.request.method)
						if (requestResponseInteraction.request.query) {
							url(requestResponseInteraction.request.path) {
								queryParameters {
									requestResponseInteraction.request.query.each { String key, List<String> value ->
										value.each { String singleValue ->
											parameter(key, singleValue)
										}
									}
								}
							}
						} else {
							url(requestResponseInteraction.request.path)
						}
						if (requestResponseInteraction.request.headers) {
							headers {
								requestResponseInteraction.request.headers.each { String key, String value ->
									header(key, value)
								}
							}
						}
						if (requestResponseInteraction.request.body.state == OptionalBody.State.PRESENT) {
							def parsedBody = BasePact.parseBody(requestResponseInteraction.request)
							if (parsedBody instanceof Map) {
								body(parsedBody as Map)
							} else if (parsedBody instanceof List) {
								body(parsedBody as List)
							} else {
								body(parsedBody.toString())
							}
						}
						if (requestResponseInteraction.request?.matchingRules) {
							stubMatchers {
								requestResponseInteraction.request.matchingRules.each { String key, Map<String, Object> value ->
									String keyFromBody = toKeyStartingFromBody(key)
									if (value.containsKey(MATCH_KEY)) {
										MatchingType matchingType = MatchingType.valueOf((value.get(MATCH_KEY) as String).toUpperCase())
										switch (matchingType) {
											case MatchingType.EQUALITY:
												// equality is checked by default in the standard way
												break
											case MatchingType.DATE:
												jsonPath(keyFromBody, byDate())
												break
											case MatchingType.TIME:
												jsonPath(keyFromBody, byTime())
												break
											case MatchingType.TIMESTAMP:
												jsonPath(keyFromBody, byTimestamp())
												break
											case MatchingType.REGEX:
												jsonPath(keyFromBody, byRegex(value.get(REGEX_KEY) as String))
												break
										}
									} else if (value.containsKey(REGEX_KEY)) {
										jsonPath(keyFromBody, byRegex(value.get(REGEX_KEY) as String))
									}
								}
							}
						}
					}
					response {
						status(requestResponseInteraction.response.status)
						if (requestResponseInteraction.response.body.state == OptionalBody.State.PRESENT) {
							def parsedBody = BasePact.parseBody(requestResponseInteraction.response)
							if (parsedBody instanceof Map) {
								body(parsedBody as Map)
							} else if (parsedBody instanceof List) {
								body(parsedBody as List)
							} else {
								body(parsedBody.toString())
							}
						}
						if (requestResponseInteraction.response?.matchingRules) {
							testMatchers {
								requestResponseInteraction.response.matchingRules.each { String key, Map<String, Object> value ->
									String keyFromBody = toKeyStartingFromBody(key)
									if (value.containsKey(MATCH_KEY)) {
										MatchingType matchingType = MatchingType.valueOf((value.get(MATCH_KEY) as String).toUpperCase())
										switch (matchingType) {
											case MatchingType.EQUALITY:
												// equality is checked by default in the standard way
												break
											case MatchingType.DATE:
												jsonPath(keyFromBody, byDate())
												break
											case MatchingType.TIME:
												jsonPath(keyFromBody, byTime())
												break
											case MatchingType.TIMESTAMP:
												jsonPath(keyFromBody, byTimestamp())
												break
											case MatchingType.REGEX:
												jsonPath(keyFromBody, byRegex(value.get(REGEX_KEY) as String))
												break
											case MatchingType.TYPE:
												jsonPath(keyFromBody, byType() {
													if (value.containsKey(MIN_KEY)) {
														minOccurrence(value.get(MIN_KEY) as Integer)
													}
													if (value.containsKey(MAX_KEY)) {
														maxOccurrence(value.get(MAX_KEY) as Integer)
													}
												})
												break
										}
									} else if (value.containsKey(REGEX_KEY)) {
										jsonPath(keyFromBody, byRegex(value.get(REGEX_KEY) as String))
									}
								}
							}
						}
						requestResponseInteraction.response.headers?.each { String key, String value ->
							headers {
								header(key, value)
							}
						}
					}
				}
			}
		}
	}

	protected String providerState(Interaction interaction) {
		return interaction.providerState ? " ${interaction.providerState}" : ""
	}

	protected String toKeyStartingFromBody(String key) {
		return key.replace('$.body', '$')
	}

	@Override
	Pact convertTo(Collection<Contract> contract) {
		Provider provider = new Provider()
		provider.name = "Provider"
		Consumer consumer = new Consumer()
		consumer.name = "Consumer"
		List<RequestResponseInteraction> interactions = contract.find { it.request }.collect { Contract dsl ->
			RequestResponseInteraction interaction = new RequestResponseInteraction()
			interaction.description = dsl.description ?: ""
			Request request = new Request().with {
				method = dsl.request.method.serverValue.toString()
				path = url(dsl)
				QueryParameters params = queryParams(dsl)
				if (params) {
					query = params.parameters.collectEntries {
						String name = it.name
						String value = it.serverValue
						return [(name) : [value]]
					}
				}
				if (dsl.request.headers) {
					headers = headers(dsl.request.headers, { DslProperty property -> property.serverValue })
				}
				if (dsl.request.body) {
					assertInputContract(dsl.request.body.serverValue)
					def json = MapConverter.getTestSideValues(dsl.request.body.serverValue)
					String jsonBody = JsonOutput.toJson(json)
					body = new OptionalBody(OptionalBody.State.PRESENT, jsonBody)
				}
				if (dsl.request.matchers && dsl.request.matchers.hasMatchers()) {
					matchingRules = matchingRules(dsl.request.matchers)
				}
				return it
			}
			Response response = new Response().with {
				status = dsl.response.status.clientValue as Integer
				if (dsl.response.headers) {
					headers = headers(dsl.response.headers, { DslProperty property -> property.clientValue })
				}
				if (dsl.response.body) {
					assertInputContract(dsl.response.body.clientValue)
					def json = MapConverter.getStubSideValues(dsl.response.body.clientValue)
					String jsonBody = JsonOutput.toJson(json)
					body = new OptionalBody(OptionalBody.State.PRESENT, jsonBody)
				}
				if (dsl.response.matchers && dsl.response.matchers.hasMatchers()) {
					matchingRules = matchingRules(dsl.response.matchers)
				}
				return it
			}
			interaction.request = request
			interaction.response = response
			return interaction
		}
		return new RequestResponsePact(provider, consumer, interactions)
	}

	protected void assertInputContract(parsedJson) {
		boolean hasExecutionProp = false
		MapConverter.transformValues(parsedJson, {
			if (it instanceof ExecutionProperty) {
				hasExecutionProp = true
			}
			return it
		})
		if (hasExecutionProp) {
			throw new UnsupportedOperationException("We can't convert a contract that has execution property")
		}
	}

	protected Map<String, String> headers(Headers headers, Closure closure) {
		return headers.entries.collectEntries {
			String name = it.name
			String value = closure(it)
			return [(name) : value]
		}
	}

	protected Map<String, Map<String, Object>> matchingRules(BodyMatchers bodyMatchers) {
		return bodyMatchers.jsonPathMatchers().collectEntries {
			MatchingType matchingType = it.matchingType()
			String key = it.path()
			Object value = it.value()
			Integer minTypeOccurrence = it.minTypeOccurrence()
			Integer maxTypeOccurrence = it.maxTypeOccurrence()
			Map<String, Object> matchingRule = [:]
			switch (matchingType) {
				case MatchingType.EQUALITY:
					matchingRule << [(MATCH_KEY) : MatchingType.EQUALITY.toString().toLowerCase() as Object]
					break
				case MatchingType.TYPE:
					Map<String, Object> map = [(MATCH_KEY) : MatchingType.TYPE.toString().toLowerCase() as Object]
					if (minTypeOccurrence) map.put(MIN_KEY, minTypeOccurrence)
					if (maxTypeOccurrence) map.put(MAX_KEY, maxTypeOccurrence)
					matchingRule << map
					break
				case MatchingType.DATE:
				case MatchingType.TIME:
				case MatchingType.TIMESTAMP:
				case MatchingType.REGEX:
					matchingRule << [
							(MATCH_KEY) : MatchingType.REGEX.toString().toLowerCase() as Object,
							(REGEX_KEY) : value
					]
					break
			}
			return [(key) : matchingRule]
		}
	}

	protected String url(Contract dsl) {
		if (dsl.request.urlPath) {
			return dsl.request.urlPath.serverValue.toString()
		} else if (dsl.request.url) {
			return dsl.request.url.serverValue.toString()
		}
		throw new IllegalStateException("No url provided")
	}

	protected QueryParameters queryParams(Contract dsl) {
		if (dsl.request.urlPath) {
			return dsl.request.urlPath.queryParameters
		} else if (dsl.request.url) {
			return dsl.request.url.queryParameters
		}
		throw new IllegalStateException("No url provided")
	}
}
