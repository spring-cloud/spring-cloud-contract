package io.codearte.accurest.wiremock

import groovy.json.JsonSlurper
import io.coderate.accurest.dsl.GroovyDsl

class WiremockToDslConverter {
    static String fromWiremockStub(String wiremockStringStub) {
        return new WiremockToDslConverter().convertFromWiremockStub(wiremockStringStub)
    }

    private String convertFromWiremockStub(String wiremockStringStub) {
        Object wiremockStub = new JsonSlurper().parseText(wiremockStringStub)
        def request = wiremockStub.request
        def response = wiremockStub.response
        return """\
            request {
                ${request.method ? "method '$request.method'" : ""}
                ${request.url ? "url '$request.url'" : ""}
                ${request.urlPattern ? "urlPattern '$request.urlPattern'" : ""}
                ${request.urlPath ? "urlPath '$request.urlPath'" : ""}
                ${request.headers ? """headers {
                    ${request.headers.collect {
            def assertion = it.value
            String headerName = it.key as String
            def entry = assertion.entrySet().first()
            """header('$headerName').$entry.key('$entry.value')\n"""
        }.join('')
        }
                }
                """ : ""}
            }
            response {
                ${response.status ? "status $response.status" : ""}
                ${response.body ? "body( ${response.body.entrySet().collectAll(withQuotedStringElements()).inject([:], appendToMap())})" : ""}
                ${response.headers ? """headers {
                     ${response.headers.collect { "header('$it.key': '${it.value}')\n" }.join('')}
                    }
                """ : ""}
            }
        """
    }

    private Closure withQuotedStringElements() {
        return { [(it.key): convert(it.value)] }
    }

    private Closure appendToMap() {
        return { acc, el -> acc << el }
    }

    private Object convert(Object element) {
        return element
    }

    private Object convert(String element) {
        return """ "$element" """
    }

    private Object convert(List element) {
        return element.collect { convert(it) }
    }

    private Object convert(Map element) {
        return element.collectEntries { [(it.key) : convert(it.value)] }
    }

    static int main(String[] args) {
        String rootOfFolderWithStubs = args[0]
        new File(rootOfFolderWithStubs).eachFileRecurse {
            try {
                GroovyDsl stub = fromWiremockStub(it.text)
                new File(it.parent, it.name.replaceAll('json', 'groovy')).text
            } catch (Exception e) {
                System.err.println(e)
            }

        }
    }
}
