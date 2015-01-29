package io.codearte.accurest.dsl

import groovy.json.JsonSlurper
import io.coderate.accurest.dsl.GroovyDsl
import io.coderate.accurest.dsl.WiremockStubStrategy
import spock.lang.Ignore
import spock.lang.Specification

class WiremockGroovyDslSpec extends Specification {

    // TODO: add alias instead of placeholder
    @Ignore
    def 'should convert groovy dsl stub to wiremock stub'() {
        given:
        GroovyDsl dsl = GroovyDsl.make {
            request {
                method('GET')
                urlPattern {
                    client('/[0-9]{2}')
                    server('/12')
                }
            }
            response {
                status(200)
                body {
                    withPlaceholder(client: '2015-01-14', server: '$anyInt($it)')
                    withTemplate('''
                        {
                              "date" : "$placeholder0"
                        }
                       ''')
                }
                headers {
                    Content-Type('text/plain')
                }
            }
        }
        expect:
        dsl.toWiremockClientStub() == '''
{
    "request": {
        "method": "GET",
        "urlPattern": "/[0-9]{2}"
    },
    "response": {
        "status": 200,
        "body": "2015-01-14",
        "headers": {
            "Content-Type": "text/plain"
        }
    }
}
'''
    }

    def "should generate stub with GET"() {
        given:
            GroovyDsl groovyDsl = GroovyDsl.make {
                request {
                    method("GET")
                }
            }
        when:
            String wiremockStub = new WiremockStubStrategy(groovyDsl).toWiremockClientStub()
        then:
            new JsonSlurper().parseText(wiremockStub) == new JsonSlurper().parseText('''
    {
        "request":{
            "method":"GET"
        }
    }
    ''')
    }

    def "should generate request when two elements are provided "() {
        given:
            GroovyDsl groovyDsl = GroovyDsl.make {
                request {
                    method("GET")
                    url("/sth")
                }
            }
        when:
            String wiremockStub = new WiremockStubStrategy(groovyDsl).toWiremockClientStub()
        then:
            new JsonSlurper().parseText(wiremockStub) == new JsonSlurper().parseText('''
    {
        "request":{
            "method":"GET",
            "url":"/sth"
        }
    }
    ''')
    }

    def "should generate request with urlPattern for client side"() {
        given:
            GroovyDsl groovyDsl = GroovyDsl.make {
                request {
                    urlPattern {
                        client('/^[0-9]{2}$')
                    }
                }
            }
        when:
            String wiremockStub = new WiremockStubStrategy(groovyDsl).toWiremockClientStub()
        then:
            new JsonSlurper().parseText(wiremockStub) == new JsonSlurper().parseText('''
    {
        "request":{
            "urlPattern":"/^[0-9]{2}$"
        }
    }
    ''')
    }

    def "should generate stub with urlPattern for server side"() {
        given:
            GroovyDsl groovyDsl = GroovyDsl.make {
                request {
                    urlPattern {
                        client('/^[0-9]{2}$')
                        server('/12')
                    }
                }
            }
        when:
            String wiremockStub = new WiremockStubStrategy(groovyDsl).toWiremockServerStub()
        then:
            new JsonSlurper().parseText(wiremockStub) == new JsonSlurper().parseText('''
    {
        "request":{
            "urlPattern":"/12"
        }
    }
    ''')
    }

    def "should generate stub with urlPath for client side"() {
        given:
            GroovyDsl groovyDsl = GroovyDsl.make {
                request {
                    urlPath ('/12')
                }
            }
        when:
            String wiremockStub = new WiremockStubStrategy(groovyDsl).toWiremockClientStub()
        then:
            new JsonSlurper().parseText(wiremockStub) == new JsonSlurper().parseText('''
    {
        "request":{
            "urlPath":"/12"
        }
    }
    ''')
    }

    def "should generate stub with urlPath for server side"() {
        given:
            GroovyDsl groovyDsl = GroovyDsl.make {
                request {
                    urlPath ('/12')
                }
            }
        when:
            String wiremockStub = new WiremockStubStrategy(groovyDsl).toWiremockServerStub()
        then:
            new JsonSlurper().parseText(wiremockStub) == new JsonSlurper().parseText('''
    {
        "request":{
            "urlPath":"/12"
        }
    }
    ''')
    }

    def "should generate stub with some headers section for client side"() {
        given:
            GroovyDsl groovyDsl = GroovyDsl.make {
                request {
                    headers {
                        header('Content-Type').equalTo('text/xml')
                        header('Accept').matches {
                            client('text/.*')
                            server('text/plain')
                        }
                        header('etag').doesNotMatch {
                            client('abcd.*')
                            server('abcdef')
                        }
                        header('X-Custom-Header').contains {
                            client('2134')
                            server('121345')
                        }
                    }
                }
            }
        when:
            String wiremockStub = new WiremockStubStrategy(groovyDsl).toWiremockClientStub()
        then:
            new JsonSlurper().parseText(wiremockStub) == new JsonSlurper().parseText('''
    {
        "request":{
            "headers": {
                "Content-Type": {
                    "equalTo": "text/xml"
                },
                "Accept": {
                    "matches": "text/.*"
                },
                "etag": {
                    "doesNotMatch": "abcd.*"
                },
                "X-Custom-Header": {
                    "contains": "2134"
                }
            }
        }
    }
    ''')
    }

    def "should generate stub with some headers section for server side"() {
        given:
            GroovyDsl groovyDsl = GroovyDsl.make {
                request {
                    headers {
                        header('Content-Type').equalTo('text/xml')
                        header('Accept').matches {
                            client('text/.*')
                            server('text/plain')
                        }
                        header('etag').doesNotMatch {
                            client('abcd.*')
                            server('abcdef')
                        }
                        header('X-Custom-Header').contains {
                            client('2134')
                            server('121345')
                        }
                    }
                }
            }
        when:
            String wiremockStub = new WiremockStubStrategy(groovyDsl).toWiremockServerStub()
        then:
            new JsonSlurper().parseText(wiremockStub) == new JsonSlurper().parseText('''
    {
        "request":{
            "headers": {
                "Content-Type": {
                    "equalTo": "text/xml"
                },
                "Accept": {
                    "matches": "text/plain"
                },
                "etag": {
                    "doesNotMatch": "abcdef"
                },
                "X-Custom-Header": {
                    "contains": "121345"
                }
            }
        }
    }
    ''')
    }


}
