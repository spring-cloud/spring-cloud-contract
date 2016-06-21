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

package org.springframework.cloud.contract.stubrunner.messaging.camel

import groovy.transform.PackageScope
import org.springframework.cloud.contract.verifier.util.BodyAsStringUtil
import org.apache.camel.Exchange
import org.apache.camel.Message
import org.apache.camel.Processor
import org.springframework.cloud.contract.verifier.dsl.Contract

/**
 * Sends forward a message defined in the DSL. Also removes headers from the
 * input message and provides the headers from the DSL.
 *
 * @author Marcin Grzejszczak
 */
@PackageScope
class StubRunnerCamelProcessor implements Processor {

	private final Contract groovyDsl

	StubRunnerCamelProcessor(Contract groovyDsl) {
		this.groovyDsl = groovyDsl
	}

	@Override
	void process(Exchange exchange) throws Exception {
		Message input = exchange.in
		groovyDsl.input.messageHeaders?.entries?.each {
			input.removeHeader(it.name)
		}
		if (!groovyDsl.outputMessage) {
			return
		}
		input.body = BodyAsStringUtil.extractStubValueFrom(groovyDsl.outputMessage.body)
		groovyDsl.outputMessage.headers?.entries?.each {
			input.setHeader(it.name, it.clientValue)
		}
	}
}
