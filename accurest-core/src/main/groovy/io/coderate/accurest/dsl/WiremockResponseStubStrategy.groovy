package io.coderate.accurest.dsl

import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import io.coderate.accurest.dsl.internal.Response

@CompileStatic
@PackageScope
class WiremockResponseStubStrategy extends BaseWiremockStubStrategy {

    private final Response response

    WiremockResponseStubStrategy(GroovyDsl groovyDsl) {
        this.response = groovyDsl.response
    }

    @PackageScope Map buildClientResponseContent() {
        return buildResponseContent(response,
                { response.getBody().forClientSide() },
                { buildClientHeadersSection(response.headers) })
    }

    @PackageScope Map buildServerResponseContent() {
        return buildResponseContent(response,
                { response.getBody().forServerSide() },
                { buildServerHeadersSection(response.headers) })
    }

    private Map<String, Object> buildResponseContent(Response response, Closure<Map<String, Object>> buildBody, Closure<Map> buildHeaders) {
        return [status : response.status,
                body   : buildBody(),
                headers: buildHeaders()].findAll { it.value }
    }

}
