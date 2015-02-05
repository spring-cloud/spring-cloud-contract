package io.coderate.accurest.dsl

import groovy.json.JsonOutput
import groovy.transform.CompileStatic
import io.coderate.accurest.dsl.internal.Response

@CompileStatic
class WiremockResponseStubStrategy extends BaseWiremockStubStrategy {

    Response response

    WiremockResponseStubStrategy(GroovyDsl groovyDsl) { //TODO: Or Response?
        this.response = groovyDsl.response
    }

    String toWiremockClientStub() {
        return JsonOutput.toJson(buildClientResponse(response))
    }

    private Map buildClientResponse(Response response) {
        return getResponseSection(response, { "TODO" }, { buildClientHeadersSection(response.headers) })
    }

    String toWiremockServerStub() {
        return JsonOutput.toJson(buildServerResponse(response))
    }

    private Map buildServerResponse(Response response) {
        return getResponseSection(response, { "TODO" }, { buildServerHeadersSection(response.headers) })
    }

    private Map<String, Map<String, Object>> getResponseSection(Response response, Closure<String> buildUrlPattern, Closure<Map> buildHeaders) {
        return [response: [status : response.status,
                           headers: buildHeaders()]
                .findAll { it.value }]
    }
}
