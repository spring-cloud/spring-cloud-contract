package io.codearte.accurest.stubrunner.boot

import io.codearte.accurest.messaging.AccurestMessageBuilder
import io.codearte.accurest.messaging.AccurestMessaging
import io.codearte.accurest.messaging.noop.NoOpAccurestMessageBuilder
import io.codearte.accurest.messaging.noop.NoOpAccurestMessaging
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * @author Marcin Grzejszczak
 */
@Configuration
class StubRunnerConfig {

	@Bean
	@ConditionalOnMissingBean
	AccurestMessaging noOpAccurestMessaging() {
		return new NoOpAccurestMessaging()
	}

	@Bean
	@ConditionalOnMissingBean
	AccurestMessageBuilder noOpAccurestMessageBuilder() {
		return new NoOpAccurestMessageBuilder()
	}
}
