package io.coderate.accurest.dsl

import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import io.coderate.accurest.dsl.internal.Request

@CompileStatic
@PackageScope
class WiremockRequestStubStrategy extends BaseWiremockStubStrategy {

    private final Request request

    WiremockRequestStubStrategy(GroovyDsl groovyDsl) {
        this.request = groovyDsl.request
    }

    @PackageScope Map buildClientRequestContent() {
        return buildRequestContent(request,
                { request.urlPattern?.toClientSide() },
                { buildClientHeadersSection(request.headers) })
    }

    @PackageScope Map buildServerRequestContent() {
        return buildRequestContent(request,
                { request.urlPattern?.toServerSide() },
                { buildServerHeadersSection(request.headers) })
    }

    private Map<String, Object> buildRequestContent(Request request, Closure<String> buildUrlPattern, Closure<Map> buildHeaders) {
        return [method    : request.method,
                 url       : request.url,
                 urlPattern: buildUrlPattern(),
                 urlPath   : request.urlPath,
                 headers   : buildHeaders()].findAll { it.value }
    }
}
