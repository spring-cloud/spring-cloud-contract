package org.springframework.cloud.contract.stubrunner.server;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.stream.test.binder.MessageCollector;
import org.springframework.cloud.stream.test.binder.TestSupportBinder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Configuration required to make Stub Runner server be executed at compile time
 *
 * @author Marcin Grzejszczak
 *
 * @since 1.0.0
 */
@Configuration
class StreamConfiguration {

	@Bean
	@Primary
	@ConditionalOnClass(name = "org.springframework.cloud.stream.test.binder.TestSupportBinder")
	@ConditionalOnMissingBean
	public MessageCollector messageCollector() {
		return new TestSupportBinder().messageCollector();
	}

}
