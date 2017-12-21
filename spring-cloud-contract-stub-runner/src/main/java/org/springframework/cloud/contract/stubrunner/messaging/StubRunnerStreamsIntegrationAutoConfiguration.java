package org.springframework.cloud.contract.stubrunner.messaging;

import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.integration.IntegrationAutoConfiguration;
import org.springframework.cloud.stream.test.binder.MessageCollectorAutoConfiguration;
import org.springframework.cloud.stream.test.binder.TestSupportBinderAutoConfiguration;
import org.springframework.context.annotation.Configuration;

/**
 * Supports
 * {@link org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner} by
 * loading in AutoConfigurations related to Stream and Integration only if the relevant
 * jars are in classpath.
 * 
 * @author Biju Kunjummen
 */
@Configuration
public class StubRunnerStreamsIntegrationAutoConfiguration {

	@Configuration
	@ConditionalOnClass(TestSupportBinderAutoConfiguration.class)
	@ImportAutoConfiguration(classes = { TestSupportBinderAutoConfiguration.class,
			MessageCollectorAutoConfiguration.class, IntegrationAutoConfiguration.class })
	static class StreamsRelatedAutoConfiguration {

	}

	@Configuration
	@ConditionalOnClass(IntegrationAutoConfiguration.class)
	@ImportAutoConfiguration(classes = { IntegrationAutoConfiguration.class })
	static class IntegrationRelatedAutoConfiguration {

	}
}
