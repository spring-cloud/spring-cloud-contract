package io.codearte.accurest.messaging.camel;

import io.codearte.accurest.messaging.AccurestMessageBuilder;
import io.codearte.accurest.messaging.AccurestMessaging;
import org.apache.camel.CamelContext;
import org.springframework.context.annotation.Bean;

/**
 * @author Marcin Grzejszczak
 */
public class AccurestCamelConfiguration {

	@Bean
	AccurestMessaging accurestMessaging(CamelContext context, AccurestMessageBuilder builder) {
		return new AccurestCamelMessaging(context, builder);
	}

	@Bean
	AccurestMessageBuilder accurestMessageBuilder() {
		return new AccurestCamelMessageBuilder();
	}
}
