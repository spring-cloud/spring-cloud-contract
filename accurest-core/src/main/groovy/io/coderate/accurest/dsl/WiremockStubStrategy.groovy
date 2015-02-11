package io.coderate.accurest.dsl

import groovy.json.JsonOutput
import groovy.transform.CompileStatic

@CompileStatic
class WiremockStubStrategy {

    private final WiremockRequestStubStrategy wiremockRequestStubStrategy
    private final WiremockResponseStubStrategy wiremockResponseStubStrategy

    WiremockStubStrategy(GroovyDsl groovyDsl) {
        this.wiremockRequestStubStrategy = new WiremockRequestStubStrategy(groovyDsl)
        this.wiremockResponseStubStrategy = new WiremockResponseStubStrategy(groovyDsl)
    }

    String toWiremockClientStub() {
        return JsonOutput.toJson([request: wiremockRequestStubStrategy.buildClientRequestContent(),
                                  response: wiremockResponseStubStrategy.buildClientResponseContent()])
    }

    String toWiremockServerStub() {
        return JsonOutput.toJson([request: wiremockRequestStubStrategy.buildServerRequestContent(),
                                  response: wiremockResponseStubStrategy.buildServerResponseContent()])
    }
}
