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

package org.springframework.cloud.contract.verifier.spec.pact;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import au.com.dius.pact.consumer.MessagePactBuilder;
import au.com.dius.pact.consumer.dsl.DslPart;
import au.com.dius.pact.core.model.messaging.MessagePact;
import groovy.lang.GString;
import org.apache.commons.collections.CollectionUtils;

import org.springframework.cloud.contract.spec.Contract;
import org.springframework.cloud.contract.spec.internal.DslProperty;
import org.springframework.cloud.contract.spec.internal.Header;
import org.springframework.cloud.contract.spec.internal.Headers;
import org.springframework.cloud.contract.spec.internal.Input;
import org.springframework.cloud.contract.spec.internal.OutputMessage;
import org.springframework.cloud.contract.verifier.util.ContentUtils;

/**
 * Creator of {@link MessagePact} instances.
 *
 * @author Tim Ysewyn
 * @author Stessy Delcroix
 * @since 2.0.0
 */
class MessagePactCreator {

	private static final Function<DslProperty<?>, Object> clientValueExtractor = DslProperty::getClientValue;

	MessagePact createFromContract(List<Contract> contracts) {
		if (CollectionUtils.isEmpty(contracts)) {
			return null;
		}
		Names names = NamingUtil.name(contracts.get(0));
		MessagePactBuilder pactBuilder = MessagePactBuilder.consumer(names.getConsumer())
				.hasPactWith(names.getProducer());

		for (Contract contract : contracts) {
			pactBuilder = pactBuilder.given(getGiven(contract.getInput()))
					.expectsToReceive(getOutcome(contract));
			if (contract.getOutputMessage() != null) {
				OutputMessage message = contract.getOutputMessage();
				if (message.getBody() != null) {
					DslPart pactResponseBody = BodyConverter.toPactBody(message.getBody(), clientValueExtractor);
					if (message.getBodyMatchers() != null) {
						pactResponseBody
								.setMatchers(MatchingRulesConverter.matchingRulesForBody(message.getBodyMatchers()));
					}
					pactResponseBody
							.setGenerators(ValueGeneratorConverter.extract(message, DslProperty::getServerValue));
					pactBuilder = pactBuilder.withContent(pactResponseBody);
				}
				if (message.getHeaders() != null) {
					pactBuilder = pactBuilder.withMetadata(getMetadata(message.getHeaders()));
				}
			}
		}
		return pactBuilder.toPact();
	}

	private String getGiven(Input input) {
		if (input.getTriggeredBy() != null) {
			return input.getTriggeredBy().getExecutionCommand();
		}
		else if (input.getMessageFrom() != null) {
			return "received message from " + clientValueExtractor.apply(input.getMessageFrom());
		}
		else {
			return "";
		}
	}

	private String getOutcome(Contract contract) {
		if (contract.getOutputMessage() != null) {
			OutputMessage message = contract.getOutputMessage();
			return "message sent to " + clientValueExtractor.apply(message.getSentTo());
		}
		else {
			return "assert that " + contract.getInput().getAssertThat().getExecutionCommand();
		}
	}

	private Map<String, String> getMetadata(Headers headers) {
		return headers.getEntries().stream().collect(Collectors.toMap(Header::getName, this::extractValue));
	}

	private String extractValue(Object value) {
		Object v = value;
		if (v instanceof DslProperty) {
			v = clientValueExtractor.apply((DslProperty) v);
		}
		if (v instanceof GString) {
			v = ContentUtils.extractValue((GString) v, clientValueExtractor);
		}
		if (v instanceof String) {
			return (String) v;
		}
		else {
			return v.toString();
		}
	}

}
