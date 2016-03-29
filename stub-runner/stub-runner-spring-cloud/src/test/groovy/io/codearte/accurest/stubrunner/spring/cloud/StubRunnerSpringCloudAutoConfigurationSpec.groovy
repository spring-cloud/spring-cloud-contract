package io.codearte.accurest.stubrunner.spring.cloud

import io.codearte.accurest.stubrunner.StubFinder
import org.apache.curator.test.TestingServer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.SpringApplicationContextLoader
import org.springframework.boot.test.WebIntegrationTest
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.cloud.client.loadbalancer.LoadBalanced
import org.springframework.cloud.zookeeper.discovery.ZookeeperServiceDiscovery
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.test.context.ContextConfiguration
import org.springframework.web.client.RestTemplate
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification

/**
 * @author Marcin Grzejszczak
 */
@ContextConfiguration(classes = Config, loader = SpringApplicationContextLoader)
@WebIntegrationTest(randomPort = true)
class StubRunnerSpringCloudAutoConfigurationSpec extends Specification {

	@Autowired StubFinder stubFinder
	@Autowired @LoadBalanced RestTemplate restTemplate
	@Autowired ZookeeperServiceDiscovery zookeeperServiceDiscovery
	@Autowired ConfigurableApplicationContext applicationContext
	@Shared @AutoCleanup TestingServer testingServer = new TestingServer(2181)

	def 'should make service discovery work'() {
		expect: 'WireMocks are running'
			"${stubFinder.findStubUrl('loanIssuance').toString()}/name".toURL().text == 'loanIssuance'
			"${stubFinder.findStubUrl('fraudDetectionServer').toString()}/name".toURL().text == 'fraudDetectionServer'
		and: 'Stubs can be reached via load service discovery'
			restTemplate.getForObject('http://loanIssuance/name', String) == 'loanIssuance'
			restTemplate.getForObject('http://someNameThatShouldMapFraudDetectionServer/name', String) == 'fraudDetectionServer'
	}

	def cleanup() {
		zookeeperServiceDiscovery.serviceDiscovery.close()
		applicationContext.close()
	}

	@Configuration
	@EnableAutoConfiguration
	@EnableDiscoveryClient
	static class Config {
		@Bean
		@LoadBalanced
		RestTemplate restTemplate() {
			return new RestTemplate()
		}
	}
}
