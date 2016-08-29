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

package org.springframework.cloud.contract.stubrunner.messaging.integration;

import java.util.Map;

import org.springframework.cloud.contract.spec.Contract;
import org.springframework.cloud.contract.verifier.util.BodyExtractor;
import org.springframework.integration.transformer.GenericTransformer;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;

/**
 * Sends forward a message defined in the DSL.
 *
 * @author Marcin Grzejszczak
 */
class StubRunnerIntegrationTransformer implements GenericTransformer<Message<?>, Message<?>> {

	private final Contract groovyDsl;

	StubRunnerIntegrationTransformer(Contract groovyDsl) {
		this.groovyDsl = groovyDsl;
	}

	@Override
	public Message<?> transform(Message<?> source) {
		if (this.groovyDsl.getOutputMessage()==null) {
			return source;
		}
		String payload = BodyExtractor.extractStubValueFrom(this.groovyDsl.getOutputMessage().getBody());
		Map<String, Object> headers = this.groovyDsl.getOutputMessage().getHeaders().asStubSideMap();
		return MessageBuilder.createMessage(payload, new MessageHeaders(headers));
	}
}
