/*
 *  Copyright 2013-2018 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
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
