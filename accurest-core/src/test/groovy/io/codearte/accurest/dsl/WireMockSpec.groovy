package io.codearte.accurest.dsl

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import spock.lang.Specification

import java.util.regex.Pattern

abstract class WireMockSpec extends Specification {

    void stubMappingIsValidWireMockStub(String mappingDefinition) {
        StubMapping stubMapping = StubMapping.buildFrom(mappingDefinition)
        stubMapping.request.bodyPatterns.findAll { it.matches }.every {
            Pattern.compile(it.matches)
        }
    }

}
