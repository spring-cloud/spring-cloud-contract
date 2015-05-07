package io.codearte.accurest.dsl

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import spock.lang.Specification

class WiremockSpec extends Specification {

    void stubMappingIsValidWiremockStub(String mappingDefinition) {
        StubMapping.buildFrom(mappingDefinition)
    }

}
