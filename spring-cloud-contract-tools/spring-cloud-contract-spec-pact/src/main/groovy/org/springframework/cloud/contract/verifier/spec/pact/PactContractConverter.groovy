package org.springframework.cloud.contract.verifier.spec.pact

import au.com.dius.pact.model.Pact
import au.com.dius.pact.model.PactReader
import au.com.dius.pact.model.RequestResponsePact
import au.com.dius.pact.model.v3.messaging.MessagePact
import groovy.transform.CompileStatic
import org.springframework.cloud.contract.spec.Contract
import org.springframework.cloud.contract.spec.ContractConverter

/**
 * Converter of JSON PACT file
 *
 * @author Marcin Grzejszczak
 * @author Tim Ysewyn
 * @since 1.1.0
 */
@CompileStatic
class PactContractConverter implements ContractConverter<Collection<Pact>> {

	private RequestResponseSCContractCreator requestResponseSCContractCreator = new RequestResponseSCContractCreator()
	private MessagingSCContractCreator messagingSCContractCreator = new MessagingSCContractCreator()

	private RequestResponsePactCreator requestResponsePactCreator = new RequestResponsePactCreator()
	private MessagePactCreator messagePactCreator = new MessagePactCreator()

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
		if (pact instanceof RequestResponsePact) {
			return requestResponseSCContractCreator.convertFrom(pact as RequestResponsePact)
		}
		if (pact instanceof MessagePact) {
			return messagingSCContractCreator.convertFrom(pact as MessagePact)
		}
		throw new UnsupportedOperationException("We currently don't support pact contracts of type" + pact.class.simpleName)
	}

	@Override
	Collection<Pact> convertTo(Collection<Contract> contracts) {
		List<Pact> pactContracts = new ArrayList<>()
		contracts.collect({ Contract contract ->
			if (contract.request) {
				pactContracts.add(requestResponsePactCreator.createFromContract(contract))
			}
			if (contract.input) {
				pactContracts.add(messagePactCreator.createFromContract(contract))
			}
		})
		return pactContracts
	}
}
