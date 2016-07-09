/*
 *  Copyright 2013-2016 the original author or authors.
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

package org.springframework.cloud.contract.verifier.messaging.noop;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.cloud.contract.verifier.messaging.ContractVerifierMessage;
import org.springframework.cloud.contract.verifier.messaging.ContractVerifierMessaging;

/**
 * @author Marcin Grzejszczak
 */
public class NoOpContractVerifierMessaging implements ContractVerifierMessaging {
	@Override
	public void send(ContractVerifierMessage message, String destination) {

	}

	@Override
	public void send(Object payload, Map headers, String destination) {

	}

	@Override
	public ContractVerifierMessage receiveMessage(String destination, long timeout, TimeUnit timeUnit) {
		return null;
	}

	@Override
	public ContractVerifierMessage receiveMessage(String destination) {
		return null;
	}

	@Override
	public ContractVerifierMessage create(Object o, Map headers) {
		return null;
	}

	@Override
	public ContractVerifierMessage create(Object o) {
		return null;
	}
}
