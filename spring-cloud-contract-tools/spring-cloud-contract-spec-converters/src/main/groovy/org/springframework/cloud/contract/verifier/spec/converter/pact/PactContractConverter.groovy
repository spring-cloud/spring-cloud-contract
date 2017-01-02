package org.springframework.cloud.contract.verifier.spec.converter.pact

import au.com.dius.pact.model.*
import groovy.transform.CompileStatic
import org.springframework.cloud.contract.spec.Contract
import org.springframework.cloud.contract.spec.ContractConverter

/**
 * Converter of JSON PACT
 * @author Marcin Grzejszczak
 * @since 1.1.0
 */
@CompileStatic
class PactContractConverter implements ContractConverter<Pact> {
	@Override
	boolean isAccepted(File file) {
		try {
			PactReader.loadPact(file)
			return true
		} catch (UnsupportedOperationException e) {
			return false
		}
	}

	@Override
	Collection<Contract> convertFrom(File file) {
		Pact pact = PactReader.loadPact(file)
		List<Interaction> interactions = pact.interactions
		return interactions.withIndex().collect { Interaction interaction, int index ->
			Contract.make {
				if (interaction instanceof RequestResponseInteraction) {
					RequestResponseInteraction requestResponseInteraction = (RequestResponseInteraction) interaction
					description("${defaultDescription(pact, index)}\n\n$requestResponseInteraction.description")
					request {
						method(requestResponseInteraction.request.method)
						url(requestResponseInteraction.request.path)
						requestResponseInteraction.request.headers?.each { String key, String value ->
							headers {
								header(key, value)
							}
						}

						if (requestResponseInteraction.request.body.state == OptionalBody.State.PRESENT) {
							def parsedBody = BasePact.parseBody(requestResponseInteraction.request)
							if (parsedBody instanceof Map) {
								body(parsedBody as Map)
							} else {
								body(parsedBody.toString())
							}
						}
					}
					response {
						status(requestResponseInteraction.response.status)
						if (requestResponseInteraction.response.body.state == OptionalBody.State.PRESENT) {
							body(BasePact.parseBody(requestResponseInteraction.response))
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

	protected String defaultDescription(Pact pact, int index) {
		Provider provider = pact.provider
		Consumer consumer = pact.consumer
		return """Consumer [${consumer.name}] -> provider [${provider.name}] interaction no [${index}]"""
	}

	@Override
	Pact convertTo(Collection<Contract> contract) {
		return null
	}
}
