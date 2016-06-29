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

import com.jayway.jsonpath.DocumentContext
import com.jayway.jsonpath.JsonPath
import com.toomuchcoding.jsonassert.JsonAssertion
import com.toomuchcoding.jsonassert.JsonVerifiable
import groovy.transform.PackageScope
import org.springframework.cloud.contract.spec.Contract
import org.apache.camel.Exchange
import org.apache.camel.Predicate
import org.springframework.cloud.contract.verifier.messaging.ContractVerifierObjectMapper
import org.springframework.cloud.contract.verifier.util.JsonPaths
import org.springframework.cloud.contract.verifier.util.JsonToJsonPathsConverter

import java.util.regex.Pattern

/**
 * Passes through a message that matches the one defined in the DSL
 *
 * @author Marcin Grzejszczak
 */
@PackageScope
class StubRunnerCamelPredicate implements Predicate {

	private final Contract groovyDsl
	private final ContractVerifierObjectMapper objectMapper = new ContractVerifierObjectMapper()

	StubRunnerCamelPredicate(Contract groovyDsl) {
		this.groovyDsl = groovyDsl
	}

	@Override
	boolean matches(Exchange exchange) {
		if(!headersMatch(exchange)){
			return false
		}
		Object inputMessage = exchange.in.body
		JsonPaths jsonPaths = JsonToJsonPathsConverter.transformToJsonPathWithStubsSideValues(groovyDsl.input.messageBody)
		DocumentContext parsedJson = JsonPath.parse(objectMapper.writeValueAsString(inputMessage))
		return jsonPaths.every { matchesJsonPath(parsedJson, it) }
	}

	private boolean matchesJsonPath(DocumentContext parsedJson, JsonVerifiable jsonVerifiable) {
		try {
			JsonAssertion.assertThat(parsedJson).matchesJsonPath(jsonVerifiable.jsonPath())
			return true
		} catch (Exception e) {
			return false
		}
	}

	private boolean headersMatch(Exchange exchange) {
		Map<String, Object> headers = exchange.getIn().getHeaders()
		return groovyDsl.input.messageHeaders.entries.every {
			String name = it.name
			Object value = it.clientValue
			Object valueInHeader = headers.get(name)
			return value instanceof Pattern ?
					value.matcher(valueInHeader.toString()).matches() :
					valueInHeader == value
		}
	}
}
