package io.coderate.accurest.dsl

import groovy.transform.TypeChecked
import io.coderate.accurest.dsl.internal.Headers
import io.coderate.accurest.dsl.internal.StringCustomizableProperty

import static io.coderate.accurest.dsl.internal.DelegateHelper.delegateToClosure

@TypeChecked
class Request {

    String method
    String url
    StringCustomizableProperty urlPattern
    String urlPath
    Headers headers

    void method(String method) {
        this.method = method
    }

    void url(String url) {
        this.url = url
    }

    void urlPattern(@DelegatesTo(StringCustomizableProperty) Closure closure) {
        StringCustomizableProperty urlPattern = new StringCustomizableProperty()
        this.urlPattern = urlPattern
        delegateToClosure(closure, urlPattern)
    }

    void headers(@DelegatesTo(Headers) Closure closure) {
        Headers headers = new Headers()
        this.headers = headers
        delegateToClosure(closure, headers)
    }

    void urlPath(String urlPath) {
        this.urlPath = urlPath
    }
}
