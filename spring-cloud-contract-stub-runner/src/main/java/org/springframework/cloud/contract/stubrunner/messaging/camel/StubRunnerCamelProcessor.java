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

package org.springframework.cloud.contract.stubrunner.messaging.camel;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.cloud.contract.spec.Contract;
import org.springframework.cloud.contract.spec.internal.FromFileProperty;
import org.springframework.cloud.contract.spec.internal.Header;
import org.springframework.cloud.contract.verifier.util.BodyExtractor;

/**
 * Sends forward a message defined in the DSL. Also removes headers from the input message
 * and provides the headers from the DSL.
 *
 * @author Marcin Grzejszczak
 */
class StubRunnerCamelProcessor implements Processor {

	private static final Log log = LogFactory.getLog(StubRunnerCamelProcessor.class);

	private static final String DUMMY_BEAN_URL = "bean:dummyStubRunnerProcessor";

	@Override
	public void process(Exchange exchange) {
		Message input = exchange.getIn();
		StubRunnerCamelPayload body = input.getBody(StubRunnerCamelPayload.class);
		Contract groovyDsl = body.contract;
		setStubRunnerDestinationHeader(exchange, body);
		if (groovyDsl.getInput().getMessageHeaders() != null) {
			for (Header entry : groovyDsl.getInput().getMessageHeaders().getEntries()) {
				input.removeHeader(entry.getName());
			}
		}
		if (groovyDsl.getOutputMessage() == null) {
			if (log.isDebugEnabled()) {
				log.debug("No output message provided, will not modify the body");
			}
			return;
		}
		input.setBody(outputBody(groovyDsl));
		if (groovyDsl.getOutputMessage().getHeaders() != null) {
			for (Header entry : groovyDsl.getOutputMessage().getHeaders().getEntries()) {
				input.setHeader(entry.getName(), entry.getClientValue());
			}
		}
	}

	private Object outputBody(Contract groovyDsl) {
		Object outputBody = BodyExtractor
				.extractClientValueFromBody(groovyDsl.getOutputMessage().getBody());
		if (outputBody instanceof FromFileProperty) {
			FromFileProperty property = (FromFileProperty) outputBody;
			return property.asBytes();
		}
		return BodyExtractor.extractStubValueFrom(outputBody);
	}

	private void setStubRunnerDestinationHeader(Exchange exchange,
			StubRunnerCamelPayload body) {
		boolean outputPart = body.contract.getOutputMessage() != null;
		String url = DUMMY_BEAN_URL;
		if (outputPart && body.contract.getOutputMessage().getSentTo() != null) {
			url = body.contract.getOutputMessage().getSentTo().getClientValue();
		}
		exchange.getIn().setHeader(
				StubRunnerCamelConfiguration.STUBRUNNER_DESTINATION_URL_HEADER_NAME, url);
		if (log.isDebugEnabled()) {
			log.debug("Set stub runner destination header to [" + url + "]");
		}
	}

}
