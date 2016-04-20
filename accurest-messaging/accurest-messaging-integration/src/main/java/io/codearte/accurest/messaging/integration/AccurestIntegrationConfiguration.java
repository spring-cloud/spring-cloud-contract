package io.codearte.accurest.messaging.integration;

import io.codearte.accurest.messaging.AccurestMessageBuilder;
import io.codearte.accurest.messaging.AccurestMessaging;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

/**
 * @author Marcin Grzejszczak
 */
public class AccurestIntegrationConfiguration {

	@Bean
	AccurestMessaging accurestMessaging(ApplicationContext applicationContext, AccurestMessageBuilder accurestMessageBuilder) {
		return new AccurestIntegrationMessaging(applicationContext, accurestMessageBuilder);
	}

	@Bean
	AccurestMessageBuilder accurestMessageBuilder() {
		return new AccurestIntegrationMessageBuilder();
	}
}
