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

package org.springframework.cloud.contract.verifier.dsl.internal

import groovy.transform.PackageScope
import groovy.transform.TypeChecked

import java.util.regex.Pattern

/**
 * Contains useful common methods for the DSL.
 *
 * @TypeChecked instead of @CompileStatic due to usage of double dispatch.
 * Double dispatch doesn't work if you're using @CompileStatic
 *
 * @since 1.0.0
 */
@TypeChecked
@PackageScope
class Common {

	@Delegate private final RegexPatterns regexPatterns = new RegexPatterns()

	Map<String, DslProperty> convertObjectsToDslProperties(Map<String, Object> body) {
		return body.collectEntries {
			Map.Entry<String, Object> entry ->
				[(entry.key): toDslProperty(entry.value)]
		} as Map<String, DslProperty>
	}

	Collection convertObjectsToDslProperties(List body) {
		return (body.collect {
			Object element -> toDslProperty(element)
		} as List)
	}

	DslProperty toDslProperty(Object property) {
		return new DslProperty(property)
	}

	DslProperty toDslProperty(Map property) {
		return new DslProperty(property.collectEntries {
			[(it.key): toDslProperty(it.value)]
		})
	}

	DslProperty toDslProperty(List property) {
		return new DslProperty(property.collect {
			toDslProperty(it)
		})
	}

	DslProperty toDslProperty(DslProperty property) {
		return property
	}

	NamedProperty named(DslProperty name, DslProperty value){
		return new NamedProperty(name, value)
	}

	NamedProperty named(Map<String, DslProperty> namedMap){
		return new NamedProperty(namedMap)
	}

	DslProperty value(ClientDslProperty client, ServerDslProperty server) {
		assertThatSidesMatch(client.clientValue, server.serverValue)
		return new DslProperty(client.clientValue, server.serverValue)
	}

	DslProperty value(Object value) {
		return new DslProperty(value)
	}

	DslProperty value(ServerDslProperty server, ClientDslProperty client) {
		assertThatSidesMatch(client.clientValue, server.serverValue)
		return new DslProperty(client.clientValue, server.serverValue)
	}

	DslProperty $(ClientDslProperty client, ServerDslProperty server) {
		return value(client, server)
	}

	DslProperty $(ServerDslProperty server, ClientDslProperty client) {
		return value(server, client)
	}

	Pattern regex(String regex) {
		return Pattern.compile(regex)
	}

	OptionalProperty optional(Object object) {
		return new OptionalProperty(object)
	}

	ExecutionProperty execute(String commandToExecute) {
		return new ExecutionProperty(commandToExecute)
	}

	ClientDslProperty client(Object clientValue) {
		return new ClientDslProperty(clientValue)
	}

	ClientDslProperty stub(Object clientValue) {
		return new ClientDslProperty(clientValue)
	}

	ServerDslProperty server(Object serverValue) {
		return new ServerDslProperty(serverValue)
	}

	ServerDslProperty test(Object serverValue) {
		return new ServerDslProperty(serverValue)
	}

	void assertThatSidesMatch(OptionalProperty stubSide, Object testSide) {
		assert testSide ==~ Pattern.compile(stubSide.optionalPattern())
	}

	void assertThatSidesMatch(Pattern pattern, String value) {
		assert value ==~ pattern
	}

	void assertThatSidesMatch(String value, Pattern pattern) {
		assert value ==~ pattern
	}

	void assertThatSidesMatch(MatchingStrategy firstSide, MatchingStrategy secondSide) {
		if (firstSide.type == MatchingStrategy.Type.ABSENT && secondSide != MatchingStrategy.Type.ABSENT) {
			throwAbsentError()
		}
	}

	void assertThatSidesMatch(MatchingStrategy firstSide, Object secondSide) {
		if (firstSide.type == MatchingStrategy.Type.ABSENT) {
			throwAbsentError()
		}
	}

	void assertThatSidesMatch(Object firstSide, MatchingStrategy secondSide) {
		if (secondSide.type == MatchingStrategy.Type.ABSENT) {
			throwAbsentError()
		}
	}

	private void throwAbsentError() {
		throw new IllegalStateException("Absent cannot only be used only on one side")
	}

	void assertThatSidesMatch(Object firstSide, Object secondSide) {
		// do nothing
	}
}
