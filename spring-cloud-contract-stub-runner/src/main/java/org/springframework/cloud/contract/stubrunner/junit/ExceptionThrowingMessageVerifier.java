/*
 * Copyright 2018-2020 the original author or authors.
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

package org.springframework.cloud.contract.stubrunner.junit;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.cloud.contract.verifier.converter.YamlContract;
import org.springframework.cloud.contract.verifier.messaging.MessageVerifier;

/**
 * @author Olga Maciaszek-Sharma
 * @since 2.1.0
 */
class ExceptionThrowingMessageVerifier implements MessageVerifier {

	private static final String EXCEPTION_MESSAGE = "Please provide a custom MessageVerifier to use this feature";

	@Override
	public void send(Object message, String destination, YamlContract contract) {
		throw new UnsupportedOperationException(EXCEPTION_MESSAGE);
	}

	@Override
	public Object receive(String destination, long timeout, TimeUnit timeUnit,
			YamlContract contract) {
		throw new UnsupportedOperationException(EXCEPTION_MESSAGE);
	}

	@Override
	public Object receive(String destination, YamlContract contract) {
		throw new UnsupportedOperationException(EXCEPTION_MESSAGE);
	}

	@Override
	public void send(Object payload, Map headers, String destination,
			YamlContract contract) {
		throw new UnsupportedOperationException(EXCEPTION_MESSAGE);
	}

}
