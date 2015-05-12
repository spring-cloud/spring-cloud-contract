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

	Map<String, DslProperty> convertObjectsToDslProperties(Map<String, Object> body) {
		return body.collectEntries {
			Map.Entry<String, Object> entry ->
				[(entry.key): toDslProperty(entry.value)]
		} as Map<String, DslProperty>
	}

	List convertObjectsToDslProperties(List body) {
		return body.collect {
			Object element -> toDslProperty(element)
		} as List
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

	DslProperty value(ClientDslProperty client, ServerDslProperty server) {
		assertThatSidesMatch(client.clientValue, server.serverValue)
		return new DslProperty(client.clientValue, server.serverValue)
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

	ExecutionProperty execute(String commandToExecute) {
		return new ExecutionProperty(commandToExecute)
	}

	ClientDslProperty client(Object clientValue) {
		return new ClientDslProperty(clientValue)
	}

	ServerDslProperty server(Object serverValue) {
		return new ServerDslProperty(serverValue)
	}

	void assertThatSidesMatch(Pattern firstSide, String secondSide) {
		assert secondSide ==~ firstSide
	}

	void assertThatSidesMatch(String firstSide, Pattern secondSide) {
		assert firstSide ==~ secondSide
	}

	void assertThatSidesMatch(Object firstSide, Object secondSide) {
		// do nothing
	}
}
