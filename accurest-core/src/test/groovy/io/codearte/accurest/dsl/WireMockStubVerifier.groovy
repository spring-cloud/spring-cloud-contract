package io.codearte.accurest.dsl

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import io.codearte.accurest.file.Contract

import java.util.regex.Pattern

trait WireMockStubVerifier {

	void stubMappingIsValidWireMockStub(String mappingDefinition) {
		StubMapping stubMapping = StubMapping.buildFrom(mappingDefinition)
		stubMapping.request.bodyPatterns.findAll { it.matches }.every {
			Pattern.compile(it.matches)
		}
		assert !mappingDefinition.contains('io.codearte.accurest.dsl.internal')
	}

	void stubMappingIsValidWireMockStub(GroovyDsl contractDsl) {
		stubMappingIsValidWireMockStub(new WireMockStubStrategy("Test", new Contract(null, false, 0, null), contractDsl).toWireMockClientStub())
	}

}
