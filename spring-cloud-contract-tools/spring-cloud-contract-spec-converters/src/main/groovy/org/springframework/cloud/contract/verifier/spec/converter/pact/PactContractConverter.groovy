package org.springframework.cloud.contract.verifier.spec.converter.pact

import au.com.dius.pact.model.*
import groovy.transform.CompileStatic
import org.springframework.cloud.contract.spec.Contract
import org.springframework.cloud.contract.spec.ContractConverter
import org.springframework.cloud.contract.spec.internal.MatchingType
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
		if (!isPactOnClasspath()) {
			return false
		}
		try {
			PactReader.loadPact(file)
			return true
		} catch (Exception e) {
			return false
		}
	}

	private boolean isPactOnClasspath() {
		try {
			Class.forName("au.com.dius.pact.model.Pact")
			return true
		} catch (ClassNotFoundException e) {
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
		return null
	}
}
