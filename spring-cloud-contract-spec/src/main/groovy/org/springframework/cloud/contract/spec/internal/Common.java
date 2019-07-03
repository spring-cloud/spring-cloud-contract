/*
 * Copyright 2013-2019 the original author or authors.
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

package org.springframework.cloud.contract.spec.internal;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Contains useful common methods for the DSL.
 *
 * @since 1.0.0
 */
public class Common {

	private final RegexPatterns regexPatterns = new RegexPatterns();

	public Map<String, DslProperty> convertObjectsToDslProperties(
			Map<String, Object> body) {
		return body.entrySet().stream().collect(Collectors.toMap(
				(Function<Map.Entry, String>) t -> t.getKey().toString(),
				(Function<Map.Entry, DslProperty>) t -> toDslProperty(t.getValue())));
	}

	public Collection convertObjectsToDslProperties(List<Object> body) {
		return body.stream().map(this::toDslProperty).collect(Collectors.toList());
	}

	public DslProperty toDslProperty(Object property) {
		return new DslProperty(property);
	}

	public DslProperty toDslProperty(Map property) {
		return new DslProperty(convertObjectsToDslProperties(property));
	}

	public DslProperty toDslProperty(List property) {
		return new DslProperty(convertObjectsToDslProperties(property));
	}

	public DslProperty toDslProperty(DslProperty property) {
		return property;
	}

	public NamedProperty named(DslProperty name, DslProperty value) {
		return new NamedProperty(name, value);
	}

	public NamedProperty named(DslProperty name, DslProperty value,
			DslProperty contentType) {
		return new NamedProperty(name, value, contentType);
	}

	public NamedProperty named(Map<String, DslProperty> namedMap) {
		return new NamedProperty(namedMap);
	}

	public DslProperty value(DslProperty value) {
		return value;
	}

	public DslProperty $(DslProperty value) {
		return value;
	}

	public DslProperty value(Object value) {
		return new DslProperty(value);
	}

	public DslProperty $(Object value) {
		return new DslProperty(value);
	}

	public DslProperty value(ClientDslProperty client, ServerDslProperty server) {
		assertThatSidesMatch(client.getClientValue(), server.getServerValue());
		return new DslProperty(client.getClientValue(), server.getServerValue());
	}

	public DslProperty $(ClientDslProperty client, ServerDslProperty server) {
		return value(client, server);
	}

	public DslProperty value(ServerDslProperty server, ClientDslProperty client) {
		assertThatSidesMatch(client.getClientValue(), server.getServerValue());
		return new DslProperty(client.getClientValue(), server.getServerValue());
	}

	public DslProperty $(ServerDslProperty server, ClientDslProperty client) {
		return value(server, client);
	}

	public RegexProperty regex(String regex) {
		return regexProperty(Pattern.compile(regex));
	}

	public RegexProperty regex(RegexProperty regex) {
		return regex;
	}

	public RegexProperty regex(Pattern regex) {
		return regexProperty(regex);
	}

	public OptionalProperty optional(Object object) {
		return new OptionalProperty(object);
	}

	public RegexProperty regexProperty(Object object) {
		return new RegexProperty(object);
	}

	public ExecutionProperty execute(String commandToExecute) {
		return new ExecutionProperty(commandToExecute);
	}

	public ClientDslProperty client(Object clientValue) {
		return new ClientDslProperty(clientValue);
	}

	/**
	 * Helper method to provide a better name for the consumer side.
	 */
	public ClientDslProperty stub(Object clientValue) {
		return new ClientDslProperty(clientValue);
	}

	/**
	 * Helper method to provide a better name for the consumer side.
	 */
	public ClientDslProperty consumer(Object clientValue) {
		return new ClientDslProperty(clientValue);
	}

	public ServerDslProperty server(Object serverValue) {
		return new ServerDslProperty(serverValue);
	}

	/**
	 * Helper method to provide a better name for the consumer side
	 */
	public ClientDslProperty c(Object clientValue) {
		return new ClientDslProperty(clientValue);
	}

	public ServerDslProperty p(Object serverValue) {
		return new ServerDslProperty(serverValue);
	}

	/**
	 * Helper method to provide a better name for the producer side.
	 */
	public ServerDslProperty test(Object serverValue) {
		return new ServerDslProperty(serverValue);
	}

	/**
	 * Read file contents as String.
	 * @param relativePath of the file to read
	 * @return String file contents
	 */
	public FromFileProperty file(String relativePath) {
		return file(relativePath, Charset.defaultCharset());
	}

	/**
	 * Read file contents as bytes[].
	 * @param relativePath of the file to read
	 * @return String file contents
	 */
	public FromFileProperty fileAsBytes(String relativePath) {
		return new FromFileProperty(fileLocation(relativePath), Byte[].class);
	}

	/**
	 * Read file contents as String with the given Charset.
	 * @param relativePath of the file to read
	 * @param charset to use for converting the bytes to String
	 * @return String file contents
	 */
	public FromFileProperty file(String relativePath, Charset charset) {
		return new FromFileProperty(fileLocation(relativePath), String.class, charset);
	}

	/**
	 * Read file contents as array of bytes.
	 * @param relativePath of the file to read
	 * @return file contents as an array of bytes
	 */
	private File fileLocation(String relativePath) {
		URL resource = Thread.currentThread().getContextClassLoader()
				.getResource(relativePath);
		if (resource == null) {
			throw new IllegalStateException("File [" + relativePath + "] is not present");
		}
		try {
			return new File(resource.toURI());
		}
		catch (URISyntaxException ex) {
			throw new IllegalStateException(ex);
		}
	}

	/**
	 * Helper method to provide a better name for the producer side.
	 */
	public ServerDslProperty producer(Object clientValue) {
		return new ServerDslProperty(clientValue);
	}

	private void throwAbsentError() {
		throw new IllegalStateException("Absent cannot only be used only on one side");
	}

	private void assertThat(boolean condition, String msg) {
		if (!condition) {
			throw new IllegalStateException(msg);
		}

	}

	public void assertThatSidesMatch(Object firstSide, Object secondSide) {
		if (firstSide instanceof OptionalProperty) {
			assertThat(
					secondSide.toString()
							.matches(((OptionalProperty) firstSide).optionalPattern()),
					"Pattern is not matched");
		}
		else if (firstSide instanceof Pattern && secondSide instanceof String) {
			assertThat(((String) secondSide).toString()
					.matches(((Pattern) firstSide).pattern()), "Pattern is not matched");
		}
		else if (secondSide instanceof Pattern && firstSide instanceof String) {
			assertThat(((String) firstSide).toString()
					.matches(((Pattern) secondSide).pattern()), "Pattern is not matched");
		}
		else if (firstSide instanceof MatchingStrategy
				&& secondSide instanceof MatchingStrategy) {
			if (((MatchingStrategy) firstSide).getType()
					.equals(MatchingStrategy.Type.ABSENT)
					&& !((MatchingStrategy) secondSide).getType()
							.equals(MatchingStrategy.Type.ABSENT)) {
				throwAbsentError();
			}

		}
		else if (firstSide instanceof MatchingStrategy) {
			if (((MatchingStrategy) firstSide).getType()
					.equals(MatchingStrategy.Type.ABSENT)) {
				throwAbsentError();
			}

		}
		else if (secondSide instanceof MatchingStrategy) {
			if (((MatchingStrategy) secondSide).getType()
					.equals(MatchingStrategy.Type.ABSENT)) {
				throwAbsentError();
			}

		}

	}

	public RegexProperty onlyAlphaUnicode() {
		return regexPatterns.onlyAlphaUnicode();
	}

	public RegexProperty alphaNumeric() {
		return regexPatterns.alphaNumeric();
	}

	public RegexProperty number() {
		return regexPatterns.number();
	}

	public RegexProperty positiveInt() {
		return regexPatterns.positiveInt();
	}

	public RegexProperty anyBoolean() {
		return regexPatterns.anyBoolean();
	}

	public RegexProperty anInteger() {
		return regexPatterns.anInteger();
	}

	public RegexProperty aDouble() {
		return regexPatterns.aDouble();
	}

	public RegexProperty ipAddress() {
		return regexPatterns.ipAddress();
	}

	public RegexProperty hostname() {
		return regexPatterns.hostname();
	}

	public RegexProperty email() {
		return regexPatterns.email();
	}

	public RegexProperty url() {
		return regexPatterns.url();
	}

	public RegexProperty httpsUrl() {
		return regexPatterns.httpsUrl();
	}

	public RegexProperty uuid() {
		return regexPatterns.uuid();
	}

	public RegexProperty isoDate() {
		return regexPatterns.isoDate();
	}

	public RegexProperty isoDateTime() {
		return regexPatterns.isoDateTime();
	}

	public RegexProperty isoTime() {
		return regexPatterns.isoTime();
	}

	public RegexProperty iso8601WithOffset() {
		return regexPatterns.iso8601WithOffset();
	}

	public RegexProperty nonEmpty() {
		return regexPatterns.nonEmpty();
	}

	public RegexProperty nonBlank() {
		return regexPatterns.nonBlank();
	}

}
