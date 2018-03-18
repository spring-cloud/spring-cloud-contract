package org.springframework.cloud.contract.verifier.spec.pact

import au.com.dius.pact.model.v3.messaging.MessagePact
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import org.springframework.cloud.contract.spec.Contract

/**
 * Creator of {@link Contract} instances
 *
 * @author Tim Ysewyn
 * @since 2.0.0
 */
@CompileStatic
@PackageScope
class MessagingSCContractCreator {

	Collection<Contract> convertFrom(MessagePact pact) {
		throw new UnsupportedOperationException("Messaging is not yet supported!")
	}

}