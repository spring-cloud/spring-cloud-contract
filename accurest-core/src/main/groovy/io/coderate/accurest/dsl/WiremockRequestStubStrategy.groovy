package io.coderate.accurest.dsl

import groovy.json.JsonOutput
import groovy.transform.CompileStatic
import io.coderate.accurest.dsl.internal.Request

@CompileStatic
class WiremockRequestStubStrategy extends BaseWiremockStubStrategy {

    private final Request request

    WiremockRequestStubStrategy(GroovyDsl groovyDsl) {
        this.request = groovyDsl.request
    }

    String toWiremockClientStub() {
        return JsonOutput.toJson(buildClientRequest(request))
    }

    String toWiremockServerStub() {
        return JsonOutput.toJson(buildServerRequest(request))
    }

    private Map buildClientRequest(Request request) {
        return getRequestSection(request,
                { request.urlPattern?.toClientSide() },
                { buildClientHeadersSection(request.headers) })
    }

    private Map buildServerRequest(Request request) {
        return getRequestSection(request,
                { request.urlPattern?.toServerSide() },
                { buildServerHeadersSection(request.headers) })
    }

    private Map<String, Map<String, Object>> getRequestSection(Request request, Closure<String> buildUrlPattern, Closure<Map> buildHeaders) {
        return [request: [method    : request.method,
                          url       : request.url,
                          urlPattern: buildUrlPattern(),
                          urlPath   : request.urlPath,
                          headers   : buildHeaders()].findAll { it.value }]
    }
}
