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

package org.springframework.cloud.contract.stubrunner.messaging.integration;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.cloud.contract.spec.Contract;
import org.springframework.cloud.contract.spec.internal.FromFileProperty;
import org.springframework.cloud.contract.verifier.util.BodyExtractor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;

/**
 * Sends forward a message defined in the DSL.
 *
 * @author Marcin Grzejszczak
 */
class StubRunnerIntegrationTransformer {

	private final StubRunnerIntegrationMessageSelector selector;

	StubRunnerIntegrationTransformer(Contract groovyDsl) {
		this(Collections.singletonList(groovyDsl));
	}

	StubRunnerIntegrationTransformer(List<Contract> groovyDsls) {
		this.selector = new StubRunnerIntegrationMessageSelector(groovyDsls);
	}

	public Message<?> transform(Message<?> source) {
		Contract groovyDsl = matchingContract(source);
		if (groovyDsl == null || groovyDsl.getOutputMessage() == null) {
			return source;
		}
		Object outputBody = outputBody(groovyDsl);
		Map<String, Object> headers = groovyDsl.getOutputMessage().getHeaders().asStubSideMap();
		MessageHeaders messageHeaders = new MessageHeaders(headers);
		Message<Object> message = MessageBuilder.createMessage(outputBody, messageHeaders);
		this.selector.updateCache(message, groovyDsl);
		return message;
	}

	private Object outputBody(Contract groovyDsl) {
		Object outputBody = BodyExtractor.extractClientValueFromBody(groovyDsl.getOutputMessage().getBody());
		if (outputBody instanceof FromFileProperty) {
			FromFileProperty property = (FromFileProperty) outputBody;
			return property.asBytes();
		}
		return BodyExtractor.extractStubValueFrom(outputBody);
	}

	Contract matchingContract(Message<?> source) {
		return this.selector.matchingContract(source);
	}

}
