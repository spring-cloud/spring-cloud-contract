/*
 * Copyright 2013-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.contract.stubrunner.spring

import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import groovy.transform.CompileStatic
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import spock.lang.Issue
import spock.lang.Specification

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.contract.stubrunner.HttpServerStubConfiguration
import org.springframework.cloud.contract.stubrunner.StubFinder
import org.springframework.cloud.contract.stubrunner.StubNotFoundException
import org.springframework.cloud.contract.stubrunner.provider.wiremock.WireMockHttpServerStubAccessor
import org.springframework.cloud.contract.stubrunner.provider.wiremock.WireMockHttpServerStubConfigurer
import org.springframework.cloud.test.TestSocketUtils
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.test.context.ActiveProfiles

/**
 * @author Marcin Grzejszczak
 */

// Not necessary if Spring Cloud is used. TODO: make it work without this.
// tag::test[]
@SpringBootTest(classes = Config, properties = [" stubrunner.cloud.enabled=false",
        'foo=${stubrunner.runningstubs.fraudDetectionServer.port}',
        'fooWithGroup=${stubrunner.runningstubs.org.springframework.cloud.contract.verifier.stubs.fraudDetectionServer.port}'])
// tag::annotation[]
@AutoConfigureStubRunner(mappingsOutputFolder = "target/outputmappings/",
        httpServerStubConfigurer = HttpsForFraudDetection)
// end::annotation[]
@ActiveProfiles("test")
class StubRunnerConfigurationSpec extends Specification {

    @Autowired
    StubFinder stubFinder
    @Autowired
    Environment environment
    @StubRunnerPort("fraudDetectionServer")
    int fraudDetectionServerPort
    @StubRunnerPort("org.springframework.cloud.contract.verifier.stubs:fraudDetectionServer")
    int fraudDetectionServerPortWithGroupId
    @Value('${foo}')
    Integer foo

    void setupSpec() {
        System.clearProperty("stubrunner.repository.root")
        System.clearProperty("stubrunner.classifier")
        WireMockHttpServerStubAccessor.clear()
    }

    void cleanupSpec() {
        setupSpec()
    }

    def 'should mark all ports as random'() {
        expect:
        WireMockHttpServerStubAccessor.everyPortRandom()
    }

    def 'should start WireMock servers'() {
        expect: 'WireMocks are running'
        stubFinder.findStubUrl('org.springframework.cloud.contract.verifier.stubs', 'loanIssuance') != null
        stubFinder.findStubUrl('loanIssuance') != null
        stubFinder.findStubUrl('loanIssuance') == stubFinder.findStubUrl('org.springframework.cloud.contract.verifier.stubs', 'loanIssuance')
        stubFinder.findStubUrl('loanIssuance') == stubFinder.findStubUrl('org.springframework.cloud.contract.verifier.stubs:loanIssuance')
        stubFinder.findStubUrl('org.springframework.cloud.contract.verifier.stubs:loanIssuance:0.0.1-SNAPSHOT') == stubFinder.findStubUrl('org.springframework.cloud.contract.verifier.stubs:loanIssuance:0.0.1-SNAPSHOT:stubs')
        stubFinder.findStubUrl('org.springframework.cloud.contract.verifier.stubs:fraudDetectionServer') != null
        and:
        stubFinder.findAllRunningStubs().isPresent('loanIssuance')
        stubFinder.findAllRunningStubs().isPresent('org.springframework.cloud.contract.verifier.stubs', 'fraudDetectionServer')
        stubFinder.findAllRunningStubs().isPresent('org.springframework.cloud.contract.verifier.stubs:fraudDetectionServer')
        and: 'Stubs were registered'
        "${stubFinder.findStubUrl('loanIssuance').toString()}/name".toURL().text == 'loanIssuance'
        "${stubFinder.findStubUrl('fraudDetectionServer').toString()}/name".toURL().text == 'fraudDetectionServer'
        and: 'Fraud Detection is an HTTPS endpoint'
        stubFinder.findStubUrl('fraudDetectionServer').toString().startsWith("https")
    }

    def 'should throw an exception when stub is not found'() {
        when:
        stubFinder.findStubUrl('nonExistingService')
        then:
        thrown(StubNotFoundException)
        when:
        stubFinder.findStubUrl('nonExistingGroupId', 'nonExistingArtifactId')
        then:
        thrown(StubNotFoundException)
    }

    def 'should register started servers as environment variables'() {
        expect:
        environment.getProperty("stubrunner.runningstubs.loanIssuance.port") != null
        stubFinder.findAllRunningStubs().getPort("loanIssuance") == (environment.getProperty("stubrunner.runningstubs.loanIssuance.port") as Integer)
        and:
        environment.getProperty("stubrunner.runningstubs.fraudDetectionServer.port") != null
        stubFinder.findAllRunningStubs().getPort("fraudDetectionServer") == (environment.getProperty("stubrunner.runningstubs.fraudDetectionServer.port") as Integer)
        and:
        environment.getProperty("stubrunner.runningstubs.fraudDetectionServer.port") != null
        stubFinder.findAllRunningStubs().getPort("fraudDetectionServer") == (environment.getProperty("stubrunner.runningstubs.org.springframework.cloud.contract.verifier.stubs.fraudDetectionServer.port") as Integer)
    }

    def 'should be able to interpolate a running stub in the passed test property'() {
        given:
        int fraudPort = stubFinder.findAllRunningStubs().getPort("fraudDetectionServer")
        expect:
        fraudPort > 0
        environment.getProperty("foo", Integer) == fraudPort
        environment.getProperty("fooWithGroup", Integer) == fraudPort
        foo == fraudPort
    }

    @Issue("#573")
    def 'should be able to retrieve the port of a running stub via an annotation'() {
        given:
        int fraudPort = stubFinder.findAllRunningStubs().getPort("fraudDetectionServer")
        expect:
        fraudPort > 0
        fraudDetectionServerPort == fraudPort
        fraudDetectionServerPortWithGroupId == fraudPort
    }

    def 'should dump all mappings to a file'() {
        when:
        def url = stubFinder.findStubUrl("fraudDetectionServer")
        then:
        new File("target/outputmappings/", "fraudDetectionServer_${url.port}").exists()
    }

    @Configuration
    @EnableAutoConfiguration
    static class Config {}

    // tag::wireMockHttpServerStubConfigurer[]
    @CompileStatic
    static class HttpsForFraudDetection extends WireMockHttpServerStubConfigurer {

        private static final Log log = LogFactory.getLog(HttpsForFraudDetection)

        @Override
        WireMockConfiguration configure(WireMockConfiguration httpStubConfiguration, HttpServerStubConfiguration httpServerStubConfiguration) {
            if (httpServerStubConfiguration.stubConfiguration.artifactId == "fraudDetectionServer") {
                int httpsPort = TestSocketUtils.findAvailableTcpPort()
                log.info("Will set HTTPs port [" + httpsPort + "] for fraud detection server")
                return httpStubConfiguration
                        .httpsPort(httpsPort)
            }
            return httpStubConfiguration
        }
    }
    // end::wireMockHttpServerStubConfigurer[]
}
// end::test[]
