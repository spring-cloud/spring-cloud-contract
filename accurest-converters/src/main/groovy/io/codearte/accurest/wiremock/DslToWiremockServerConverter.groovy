package io.codearte.accurest.wiremock

import io.coderate.accurest.dsl.WiremockStubStrategy

class DslToWiremockServerConverter extends DslToWiremockConverter {

    @Override
    String convertContent(String dslBody) {
        return new WiremockStubStrategy(createGroovyDSLfromStringContent(dslBody)).toWiremockServerStub()
    }
}
