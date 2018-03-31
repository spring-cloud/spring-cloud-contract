/*
 *  Copyright 2013-2017 the original author or authors.
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

package org.springframework.cloud.contract.spec.internal

import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import groovy.transform.TypeChecked
import groovy.util.logging.Slf4j
import org.apache.commons.lang3.StringEscapeUtils
import repackaged.nl.flotsam.xeger.Xeger

import java.util.regex.Pattern
/**
 * Represents an output for messaging. Used for verifying
 * the body and headers that are sent.
 *
 * @author Marcin Grzejszczak
 * @author Tim Ysewyn
 * @since 1.0.0
 */
@Slf4j
@TypeChecked
@EqualsAndHashCode
@ToString(includePackage = false, includeNames = true)
class OutputMessage extends Common {

	@Delegate ServerPatternValueDslProperty property = new ServerPatternValueDslProperty()

	DslProperty<String> sentTo
	Headers headers
	DslProperty body
	ExecutionProperty assertThat
	ResponseBodyMatchers bodyMatchers

	OutputMessage() {}

	OutputMessage(OutputMessage outputMessage) {
		this.sentTo = outputMessage.sentTo
		this.headers = outputMessage.headers
		this.body = outputMessage.body
	}

	void sentTo(String sentTo) {
		this.sentTo = new DslProperty(sentTo)
	}

	void sentTo(DslProperty sentTo) {
		this.sentTo = sentTo
	}

	void body(Object bodyAsValue) {
		this.body = new DslProperty(bodyAsValue)
	}

	void body(DslProperty bodyAsValue) {
		this.body = bodyAsValue
	}

	void headers(@DelegatesTo(Headers) Closure closure) {
		this.headers = new Headers()
		closure.delegate = headers
		closure()
	}

	void assertThat(String assertThat) {
		this.assertThat = new ExecutionProperty(assertThat)
	}

	DslProperty value(ServerDslProperty server) {
		Object value = server.clientValue
		if (server.clientValue instanceof Pattern) {
			value = StringEscapeUtils.escapeJava(new Xeger(((Pattern)server.clientValue).pattern()).generate())
		}
		return new DslProperty(value, server.serverValue)
	}

	/**
	 * @deprecated Deprecated in favor of bodyMatchers to support other future bodyMatchers too
	 */
	@Deprecated
	void testMatchers(@DelegatesTo(ResponseBodyMatchers) Closure closure) {
		log.warn("testMatchers method is deprecated. Please use bodyMatchers instead")
		bodyMatchers(closure)
	}

	void bodyMatchers(@DelegatesTo(ResponseBodyMatchers) Closure closure) {
		this.bodyMatchers = new ResponseBodyMatchers()
		closure.delegate = this.bodyMatchers
		closure()
	}

	@CompileStatic
	@EqualsAndHashCode(includeFields = true)
	@ToString(includePackage = false)
	private class ServerPatternValueDslProperty extends PatternValueDslProperty<ServerDslProperty> {

		@Override
		protected ServerDslProperty createProperty(Pattern pattern, Object generatedValue) {
			return new ServerDslProperty(pattern, generatedValue)
		}
	}
}

@CompileStatic
@EqualsAndHashCode
@ToString(includePackage = false)
class ServerOutputMessage extends OutputMessage {
	ServerOutputMessage(OutputMessage request) {
		super(request)
	}
}

@CompileStatic
@EqualsAndHashCode
@ToString(includePackage = false)
class ClientOutputMessage extends OutputMessage {
	ClientOutputMessage(OutputMessage request) {
		super(request)
	}
}

