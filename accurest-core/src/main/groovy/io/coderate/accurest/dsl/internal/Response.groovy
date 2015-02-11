package io.coderate.accurest.dsl.internal

import groovy.transform.TypeChecked

import static io.coderate.accurest.dsl.internal.DelegateHelper.delegateToClosure

@TypeChecked
class Response {

    private static final String CLIENT_PROP_KEY = 'client'
    private static final String SERVER_PROP_KEY = 'server'

    private int status
    private Headers headers
    private Body body = new Body()

    void status(int status) {
        this.status = status
    }

    void headers(@DelegatesTo(Headers) Closure closure) {
        this.headers = new Headers()
        delegateToClosure(closure, headers)
    }

    void body(Map<String, Object> body) {
        this.body = new Body(convertObjectsToDslProperties(body))
    }

    private Map<String, DslProperty> convertObjectsToDslProperties(Map<String, Object> body) {
        return body.collectEntries {
            Map.Entry<String, Object> entry ->
                [(entry.key): entry.value instanceof DslProperty ? entry.value : new DslProperty(entry.value)]
        } as Map<String, DslProperty>
    }

    DslProperty property(Map<String, Object> properties) {
        return new DslProperty(properties[CLIENT_PROP_KEY], properties[SERVER_PROP_KEY])
    }

    DslProperty $(Map<String, Object> properties) {
        return property(properties)
    }

    Body getBody() {
        return body
    }

    int getStatus() {
        return status
    }

    Headers getHeaders() {
        return headers
    }
}
