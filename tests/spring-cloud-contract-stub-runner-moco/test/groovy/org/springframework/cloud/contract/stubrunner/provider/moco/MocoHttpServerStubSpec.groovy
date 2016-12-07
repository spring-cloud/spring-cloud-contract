package org.springframework.cloud.contract.stubrunner.provider.moco

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.context.SpringBootContextLoader
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.contract.stubrunner.StubFinder
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner
import org.springframework.context.annotation.Configuration
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

/**
 * @author Marcin Grzejszczak
 */
@ContextConfiguration(classes = Config, loader = SpringBootContextLoader)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureStubRunner( ids =
		["org.springframework.cloud.contract.verifier.stubs:fraudDetectionServerMoco"],
		repositoryRoot = "classpath:m2repo/repository/")
@DirtiesContext
class MocoHttpServerStubSpec extends Specification {

	@Autowired StubFinder stubFinder

	def 'should successfully receive a response from a stub'() {
		expect:
			"${stubFinder.findStubUrl('fraudDetectionServerMoco').toString()}/name".toURL().text == 'fraudDetectionServerMoco'
	}

	@Configuration
	@EnableAutoConfiguration
	static class Config {
	}
}
