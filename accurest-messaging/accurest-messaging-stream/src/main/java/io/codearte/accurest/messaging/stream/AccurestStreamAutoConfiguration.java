package io.codearte.accurest.messaging.stream;

import io.codearte.accurest.messaging.AccurestMessageBuilder;
import io.codearte.accurest.messaging.AccurestMessaging;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Marcin Grzejszczak
 */
@Configuration
public class AccurestStreamAutoConfiguration {

	@Bean
	AccurestMessaging accurestMessaging(ApplicationContext applicationContext, AccurestMessageBuilder accurestMessageBuilder) {
		return new AccurestStreamMessaging(applicationContext, accurestMessageBuilder);
	}

	@Bean
	AccurestMessageBuilder accurestMessageBuilder() {
		return new AccurestStreamMessageBuilder();
	}
}
