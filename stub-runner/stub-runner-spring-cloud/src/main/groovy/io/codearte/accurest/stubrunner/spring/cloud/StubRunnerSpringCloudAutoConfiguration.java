package io.codearte.accurest.stubrunner.spring.cloud;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

import io.codearte.accurest.stubrunner.StubFinder;
import io.codearte.accurest.stubrunner.spring.StubRunnerConfiguration;

/**
 * Wraps {@link DiscoveryClient} in a Stub Runner implementation that tries to find
 * a corresponding WireMock server for a searched dependency
 */
@Configuration
@EnableConfigurationProperties
@ConditionalOnClass(DiscoveryClient.class)
@Import(StubRunnerConfiguration.class)
public class StubRunnerSpringCloudAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	public StubMapperProperties stubMapperProperties() {
		return new StubMapperProperties();
	}

	@Bean
	@Primary
	public DiscoveryClient stubRunnerDiscoveryClient(DiscoveryClient discoveryClient,
			StubFinder stubFinder,
			StubMapperProperties stubMapperProperties) {
		return new StubRunnerDiscoveryClient(discoveryClient, stubFinder, stubMapperProperties);
	}

}
