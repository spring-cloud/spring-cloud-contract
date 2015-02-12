package io.codearte.accurest.wiremock
import groovy.json.JsonSlurper
import io.coderate.accurest.dsl.GroovyDsl

class WiremockToDslConverter {
    static GroovyDsl fromWiremockStub(String wiremockStringStub) {
        Object wiremockStub = new JsonSlurper().parseText(wiremockStringStub)
        def wiremockRequest = wiremockStub.request
        def wiremockResponse = wiremockStub.response
        return GroovyDsl.make {
            request {
                wiremockRequest.method ? method(wiremockRequest.method as String) : null
                wiremockRequest.url ? url(wiremockRequest.url as String) : null
                wiremockRequest.urlPattern ? urlPattern(wiremockRequest.urlPattern as String) : null
                wiremockRequest.urlPath ? urlPath(wiremockRequest.urlPath as String) : null
                wiremockRequest.headers ? headers {
                    wiremockRequest.headers.each {
                        def assertion = it.value
                        String headerName = it.key as String
                        header(headerName)."$assertion.key"(assertion.value)
                    }
                } : null
            }
            response {
                status wiremockResponse.status ? wiremockResponse.status as Integer : null
                wiremockResponse.body ? body (
                        wiremockResponse.body as Map
                ) : null
                wiremockResponse.headers ? headers {
                    wiremockResponse.headers.each {
                        header([(it.key) : it.value])
                    }
                } : null
            }
        }
    }
}
