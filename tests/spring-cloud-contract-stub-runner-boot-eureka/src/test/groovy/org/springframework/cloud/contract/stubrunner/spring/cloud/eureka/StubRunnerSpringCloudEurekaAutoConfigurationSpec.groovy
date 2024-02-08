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

package org.springframework.cloud.contract.stubrunner.spring.cloud.eureka

import org.springframework.cloud.netflix.eureka.http.EurekaClientHttpRequestFactorySupplier
import org.springframework.cloud.netflix.eureka.http.RestTemplateDiscoveryClientOptionalArgs
import org.springframework.cloud.netflix.eureka.http.RestTemplateTransportClientFactories

import java.util.concurrent.TimeUnit

import groovy.util.logging.Slf4j
import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.FilterConfig
import jakarta.servlet.ServletException
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import org.awaitility.Awaitility
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.client.loadbalancer.LoadBalanced
import org.springframework.cloud.contract.stubrunner.StubFinder
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.ClientHttpResponse
import org.springframework.web.client.DefaultResponseErrorHandler
import org.springframework.web.client.RestTemplate
/**
 * @author Marcin Grzejszczak
 */
//TODO: Speed up this test somehow (move it out of Spring Cloud Contract core to samples)
@SpringBootTest(classes = Config, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = ["stubrunner.cloud.eureka.enabled=true",
                "stubrunner.cloud.stubbed.discovery.enabled=false",
                "eureka.client.enabled=true",
				"debug=true",
                "eureka.instance.leaseRenewalIntervalInSeconds=1"])
@AutoConfigureStubRunner(ids = ["org.springframework.cloud.contract.verifier.stubs:loanIssuance",
 "org.springframework.cloud.contract.verifier.stubs:fraudDetectionServer", "org.springframework.cloud.contract.verifier.stubs:bootService"] ,
repositoryRoot = "classpath:m2repo/repository/" ,
stubsMode = StubRunnerProperties.StubsMode.REMOTE )
@Slf4j
class StubRunnerSpringCloudEurekaAutoConfigurationSpec {

	@Autowired
	StubFinder stubFinder

	@Autowired
	@LoadBalanced
	RestTemplate restTemplate

	static ConfigurableApplicationContext eurekaServer

	@BeforeAll
	static void setupSpec() {
		System.clearProperty("stubrunner.stubs.repository.root")
		System.clearProperty("stubrunner.stubs.classifier")
		eurekaServer = SpringApplication.run(EurekaServer,
				"--stubrunner.cloud.eureka.enabled=true",
				"--stubrunner.cloud.stubbed.discovery.enabled=false",
				"--eureka.client.enabled=true",
				"--server.port=8761",
				"--spring.profiles.active=eureka")
	}

	@AfterAll
	static void cleanupSpec() {
		System.clearProperty("stubrunner.stubs.repository.root")
		System.clearProperty("stubrunner.stubs.classifier")
	}

	@Test
	void 'should make service discovery work'() {
		expect: 'WireMocks are running'
			assert "${stubFinder.findStubUrl('loanIssuance').toString()}/name".toURL().text == 'loanIssuance'
			assert "${stubFinder.findStubUrl('fraudDetectionServer').toString()}/name".toURL().text == 'fraudDetectionServer'
		and: 'Stubs can be reached via load service discovery'
			log.info("Waiting for stubs to register in Eureka...")
			Awaitility.await()
					.pollInterval(1, TimeUnit.SECONDS)
					.pollDelay(10, TimeUnit.SECONDS)
					.atMost(1, TimeUnit.MINUTES)
					.untilAsserted(() -> {
				try {
					assert restTemplate.getForObject('http://loanIssuance/name', String) == 'loanIssuance'
				} catch (Exception ex) {
					throw new AssertionError(ex)
				}
			})
			assert restTemplate.getForObject('http://someNameThatShouldMapFraudDetectionServer/name', String) == 'fraudDetectionServer'
	}

	@Configuration
	@EnableAutoConfiguration
	static class Config {

		@Bean
		@LoadBalanced
		RestTemplate restTemplate() {
			def template = new RestTemplate()
			template.errorHandler = new DefaultResponseErrorHandler() {
				@Override
				void handleError(ClientHttpResponse response) throws IOException {
					try {
						super.handleError(response)
					}
					catch (Exception e) {
						throw new AssertionError(e)
					}
				}
			}
			return template
		}

		// because eureka server has JerseyClient, need these beans for eureka client in same jvm to work
		@Bean
		RestTemplateDiscoveryClientOptionalArgs restTemplateDiscoveryClientOptionalArgs(EurekaClientHttpRequestFactorySupplier eurekaClientHttpRequestFactorySupplier) {
			return new RestTemplateDiscoveryClientOptionalArgs(eurekaClientHttpRequestFactorySupplier);
		}

		@Bean
		RestTemplateTransportClientFactories restTemplateTransportClientFactories(
				RestTemplateDiscoveryClientOptionalArgs optionalArgs) {
			return new RestTemplateTransportClientFactories(optionalArgs);
		}
	}

	@Configuration
	@EnableAutoConfiguration
	@EnableEurekaServer
	static class EurekaServer {

		@Bean
		Filter httpTraceFilter() {
			return new Filter() {
				@Override
				void init(FilterConfig filterConfig) throws ServletException {

				}

				@Override
				void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
					filterChain.doFilter(servletRequest, servletResponse)
				}

				@Override
				void destroy() {

				}
			}
		}


	}
}
