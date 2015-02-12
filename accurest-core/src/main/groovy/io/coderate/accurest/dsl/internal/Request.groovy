package io.coderate.accurest.dsl.internal
import groovy.transform.CompileStatic
import groovy.transform.TypeChecked

import static io.coderate.accurest.dsl.internal.DelegateHelper.delegateToClosure

@TypeChecked
class Request extends Common {

    DslProperty method
    DslProperty url
    DslProperty urlPattern
    DslProperty urlPath
    Headers headers

    Request() {
    }

    Request(Request request) {
        this.method = request.method
        this.url = request.url
        this.urlPattern = request.urlPattern
        this.urlPath = request.urlPath
        this.headers = request.headers
    }

    void method(String method) {
        this.method = toDslProperty(method)
    }

    void method(DslProperty method) {
        this.method = toDslProperty(method)
    }

    void url(String url) {
        this.url = toDslProperty(url)
    }

    void url(DslProperty url) {
        this.url = toDslProperty(url)
    }

    void urlPattern(String urlPattern) {
        this.urlPattern = toDslProperty(urlPattern)
    }

    void urlPattern(DslProperty urlPattern) {
        this.urlPattern = toDslProperty(urlPattern)
    }

    void headers(@DelegatesTo(Headers) Closure closure) {
        Headers headers = new Headers()
        this.headers = headers
        delegateToClosure(closure, headers)
    }

    void urlPath(String urlPath) {
        this.urlPath = toDslProperty(urlPath)
    }

    void urlPath(DslProperty urlPath) {
        this.urlPath = toDslProperty(urlPath)
    }
}

@CompileStatic
class ServerRequest extends Request {
    ServerRequest(Request request) {
        super(request)
    }
}

@CompileStatic
class ClientRequest extends Request {
    ClientRequest(Request request) {
        super(request)
    }
}
