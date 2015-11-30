package io.codearte.accurest.dsl.internal

import groovy.transform.PackageScope
import groovy.transform.TypeChecked

import java.util.regex.Pattern

/**
 * @TypeChecked instead of @CompileStatic due to usage of double dispatch.
 * Double dispatch doesn't work if you're using @CompileStatic
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
