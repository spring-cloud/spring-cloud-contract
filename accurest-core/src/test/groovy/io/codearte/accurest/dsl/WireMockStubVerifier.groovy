package io.codearte.accurest.dsl
import com.github.tomakehurst.wiremock.stubbing.StubMapping

import java.util.regex.Pattern

trait WireMockStubVerifier {

    void stubMappingIsValidWireMockStub(String mappingDefinition) {
        StubMapping stubMapping = StubMapping.buildFrom(mappingDefinition)
        stubMapping.request.bodyPatterns.findAll { it.matches }.every {
            Pattern.compile(it.matches)
        }
        assert !mappingDefinition.contains('DslProperty')
    }

}
