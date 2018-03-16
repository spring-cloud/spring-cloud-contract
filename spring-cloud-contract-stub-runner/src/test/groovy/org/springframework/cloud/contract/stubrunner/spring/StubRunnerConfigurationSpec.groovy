/*
 *  Copyright 2013-2017 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.springframework.cloud.contract.stubrunner.spring

import org.junit.AfterClass
import org.junit.BeforeClass
import spock.lang.Issue

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.context.SpringBootContextLoader
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.contract.stubrunner.StubFinder
import org.springframework.cloud.contract.stubrunner.StubNotFoundException
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification
/**
 * @author Marcin Grzejszczak
 */

// Not necessary if Spring Cloud is used. TODO: make it work without this.
// tag::test[]
@ContextConfiguration(classes = Config, loader = SpringBootContextLoader)
@SpringBootTest(properties = [" stubrunner.cloud.enabled=false", 
		'foo=${stubrunner.runningstubs.fraudDetectionServer.port}',
		'fooWithGroup=${stubrunner.runningstubs.org.springframework.cloud.contract.verifier.stubs.fraudDetectionServer.port}'])
@AutoConfigureStubRunner(mappingsOutputFolder = "target/outputmappings/")
@DirtiesContext
@ActiveProfiles("test")
class StubRunnerConfigurationSpec extends Specification {

	@Autowired StubFinder stubFinder
	@Autowired Environment environment
	@StubRunnerPort("fraudDetectionServer") int fraudDetectionServerPort
	@StubRunnerPort("org.springframework.cloud.contract.verifier.stubs:fraudDetectionServer") int fraudDetectionServerPortWithGroupId
	@Value('${foo}') Integer foo

	@BeforeClass
	@AfterClass
	void setupProps() {
		System.clearProperty("stubrunner.repository.root")
		System.clearProperty("stubrunner.classifier")
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
}
// end::test[]