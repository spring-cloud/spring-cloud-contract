/*
 * Copyright 2013-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.contract.verifier.messaging.noop;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.cloud.contract.verifier.converter.YamlContract;

/**
 * @author Marcin Grzejszczak
 */
public class NoOpStubMessages<U>
		implements org.springframework.cloud.contract.verifier.messaging.MessageVerifierSender<U>,
		org.springframework.cloud.contract.verifier.messaging.MessageVerifierReceiver<U> {

	@Override
	public void send(U message, String destination, YamlContract contract) {
	}

	@Override
	public <T> void send(T payload, Map<String, Object> headers, String destination, YamlContract contract) {
	}

	@Override
	public U receive(String destination, long timeout, TimeUnit timeUnit, YamlContract contract) {
		return null;
	}

	@Override
	public U receive(String destination, YamlContract contract) {
		return null;
	}

}
