package io.coderate.accurest.dsl

import groovy.json.JsonOutput
import groovy.transform.CompileStatic

@CompileStatic
class WiremockStubStrategy {

    private final Request request

    WiremockStubStrategy(GroovyDsl groovyDsl) {
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

    private Map buildClientHeadersSection(Request.Headers headers) {
        return createHeadersSection(headers) {
            Map.Entry<String, Request.WithValuePattern> entry -> [(entry.key): buildClientHeaderFromValuePattern(entry.value)]
        }
    }

    private Map buildServerHeadersSection(Request.Headers headers) {
        return createHeadersSection(headers) {
            Map.Entry<String, Request.WithValuePattern> entry -> [(entry.key): buildServerHeaderFromValuePattern(entry.value)]
        }
    }

    private Map createHeadersSection(Request.Headers headers, Closure closure) {
        return headers?.entries()?.collectEntries(closure)
    }


    private Map buildClientHeaderFromValuePattern(Request.WithValuePattern valuePattern) {
        return getValuePatternSection(valuePattern)
                .findAll { it.value }
                .collectEntries { [(it.key): it.value.toClientSide()] }
    }

    private Map buildServerHeaderFromValuePattern(Request.WithValuePattern valuePattern) {
        return getValuePatternSection(valuePattern)
                .findAll { it.value }
                .collectEntries { [(it.key): it.value.toServerSide()] }
    }

    private Map<String, Request.CustomizableProperty> getValuePatternSection(Request.WithValuePattern valuePattern) {
        return [equalToJson    : valuePattern.equalToJson,
                equalToXml     : valuePattern.equalToXml,
                matchesXPath   : valuePattern.matchesXPath,
                jsonCompareMode: valuePattern.jsonCompareMode,
                equalTo        : valuePattern.equalTo,
                contains       : valuePattern.contains,
                matches        : valuePattern.matches,
                doesNotMatch   : valuePattern.doesNotMatch,
                absent         : valuePattern.absent,
                matchesJsonPath: valuePattern.matchesJsonPath]
    }

}
