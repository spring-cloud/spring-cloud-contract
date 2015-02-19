package io.codearte.accurest.wiremock

import groovy.json.JsonSlurper
import spock.lang.Specification

class DslToWiremockServerConverterSpec extends Specification {

    def "should convert DSL file to Wiremock JSON"() {
        given:
            def converter = new DslToWiremockServerConverter()
        and:
            String dslBody = """
                io.coderate.accurest.dsl.GroovyDsl.make {
                    request {
                        method('PUT')
                        urlPattern \$(client('/[0-9]{2}'), server('/12'))
                    }
                    response {
                        status 200
                    }
                }
"""
        when:
            String json = converter.convertContent(dslBody)
        then:
            new JsonSlurper().parseText(json) == new JsonSlurper().parseText("""
{"request":{"method":"PUT","urlPattern":"/12"},"response":{"status":200}}""")
    }
}
