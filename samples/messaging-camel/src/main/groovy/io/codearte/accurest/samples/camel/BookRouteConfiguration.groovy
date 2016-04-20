package io.codearte.accurest.samples.camel

import org.apache.activemq.camel.component.ActiveMQComponent
import org.apache.camel.RoutesBuilder
import org.apache.camel.model.dataformat.JsonLibrary
import org.apache.camel.spring.SpringRouteBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
/**
 * @author Marcin Grzejszczak
 */
@Configuration
class BookRouteConfiguration {

	@Bean
	ActiveMQComponent activeMQComponent(@Value('${activemq.url:vm://localhost?broker.persistent=false}') String url) {
		return new ActiveMQComponent(brokerURL: url)
	}

	@Bean
	RoutesBuilder myRouter(BookService bookService, BookDeleter bookDeleter) {
		return new SpringRouteBuilder() {

			@Override
			public void configure() throws Exception {
				// scenario 1 - from bean to output
				from("direct:start").unmarshal().json(JsonLibrary.Jackson, BookReturned).bean(bookService).to("jms:output")
				// scenario 2 - from input to output
				from("jms:input").unmarshal().json(JsonLibrary.Jackson, BookReturned).bean(bookService).to("jms:output")
				// scenario 3 - from input to no output
				from("jms:delete").unmarshal().json(JsonLibrary.Jackson, BookDeleted).bean(bookDeleter)
			}

		};
	}
}
