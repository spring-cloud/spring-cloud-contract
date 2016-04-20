package io.codearte.accurest.samples.spring.config

import io.codearte.accurest.messaging.AccurestMessaging
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jms.core.JmsTemplate
/**
 * @author Marcin Grzejszczak
 */
@Configuration
class ManualAccurestConfiguration {

	@Bean
	AccurestMessaging accurestMessaging(JmsTemplate jmsTemplate) {
		return new AccurestSpringMessaging(new AccurestSpringMessageBuilder(), jmsTemplate);
	}

}
