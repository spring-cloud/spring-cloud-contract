package io.codearte.accurest.dsl

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import spock.lang.Specification

import java.util.regex.Pattern

class WiremockSpec extends Specification {

    void stubMappingIsValidWiremockStub(String mappingDefinition) {
        StubMapping stubMapping = StubMapping.buildFrom(mappingDefinition)
        stubMapping.request.bodyPatterns.findAll { it.matches }.every {
            Pattern.compile(it.matches)
        }
    }

}
