package org.springframework.cloud.contract.stubrunner.messaging.camel;

import org.springframework.cloud.contract.spec.Contract;

/**
 * @author Marcin Grzejszczak
 */
class StubRunnerCamelPayload {
	final Object payload;
	final Contract contract;

	StubRunnerCamelPayload(Contract contract) {
		this.contract = contract;
		this.payload = null;
	}
}
