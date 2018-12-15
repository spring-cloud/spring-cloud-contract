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

import java.nio.charset.Charset

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

	NamedProperty named(DslProperty name, DslProperty value, DslProperty contentType) {
		return new NamedProperty(name, value, contentType)
	}

	NamedProperty named(Map<String, DslProperty> namedMap){
		return new NamedProperty(namedMap)
	}

	DslProperty value(DslProperty value) {
		return value
	}

	DslProperty $(DslProperty value) {
		return value
	}

	DslProperty value(Object value) {
		return new DslProperty(value)
	}

	DslProperty $(Object value) {
		return new DslProperty(value)
	}

	DslProperty value(ClientDslProperty client, ServerDslProperty server) {
		assertThatSidesMatch(client.clientValue, server.serverValue)
		return new DslProperty(client.clientValue, server.serverValue)
	}

	DslProperty $(ClientDslProperty client, ServerDslProperty server) {
		return value(client, server)
	}

	DslProperty value(ServerDslProperty server, ClientDslProperty client) {
		assertThatSidesMatch(client.clientValue, server.serverValue)
		return new DslProperty(client.clientValue, server.serverValue)
	}

	DslProperty $(ServerDslProperty server, ClientDslProperty client) {
		return value(server, client)
	}

	Pattern regex(String regex) {
		return Pattern.compile(regex)
	}

	// Backward compatibility with RegexPatterns
	Pattern regex(Pattern regex) {
		return regex
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

	/**
	 * Helper method to provide a better name for the consumer side
	 */
	ClientDslProperty stub(Object clientValue) {
		return new ClientDslProperty(clientValue)
	}

	/**
	 * Helper method to provide a better name for the consumer side
	 */
	ClientDslProperty consumer(Object clientValue) {
		return new ClientDslProperty(clientValue)
	}

	ServerDslProperty server(Object serverValue) {
		return new ServerDslProperty(serverValue)
	}

	/**
	 * Helper method to provide a better name for the consumer side
	 */
	ClientDslProperty c(Object clientValue) {
		return new ClientDslProperty(clientValue)
	}

	ServerDslProperty p(Object serverValue) {
		return new ServerDslProperty(serverValue)
	}

	/**
	 * Helper method to provide a better name for the producer side
	 */
	ServerDslProperty test(Object serverValue) {
		return new ServerDslProperty(serverValue)
	}

	/**
	 * Read file contents as String
	 *
	 * @param relativePath of the file to read
	 * @return String file contents
	 */
	FromFileProperty file(String relativePath) {
		return file(relativePath, Charset.defaultCharset())
	}

	/**
	 * Read file contents as bytes[]
	 *
	 * @param relativePath of the file to read
	 * @return String file contents
	 */
	FromFileProperty fileAsBytes(String relativePath) {
		return new FromFileProperty(fileLocation(relativePath), byte[])
	}

	/**
	 * Read file contents as String with the given Charset
	 *
	 * @param relativePath of the file to read
	 * @param charset to use for converting the bytes to String
	 * @return String file contents
	 */
	FromFileProperty file(String relativePath, Charset charset) {
		return new FromFileProperty(fileLocation(relativePath), String, charset)
	}

	/**
	 * Read file contents as array of bytes
	 *
	 * @param relativePath of the file to read
	 * @return file contents as an array of bytes
	 */
	private File fileLocation(String relativePath) {
		URL resource = Thread.currentThread().getContextClassLoader().getResource(relativePath)
		if (resource == null) {
			throw new IllegalStateException("File [${relativePath}] is not present")
		}
		return new File(resource.toURI())
	}

	/**
	 * Helper method to provide a better name for the producer side
	 */
	ServerDslProperty producer(Object clientValue) {
		return new ServerDslProperty(clientValue)
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