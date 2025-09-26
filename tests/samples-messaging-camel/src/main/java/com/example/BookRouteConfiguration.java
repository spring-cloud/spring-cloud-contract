/*
 * Copyright 2013-present the original author or authors.
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

package com.example;

import org.apache.camel.CamelContext;
import org.apache.camel.RoutesBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.springrabbit.SpringRabbitMQComponent;
import org.apache.camel.model.dataformat.JsonLibrary;

import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Marcin Grzejszczak
 */
@Configuration(proxyBeanMethods = false)
public class BookRouteConfiguration {

	@Bean
	RoutesBuilder myRouter(final BookService bookService, CamelContext context, ConnectionFactory connectionFactory) {
		return new RouteBuilder() {

			@Override
			public void configure() throws Exception {
				SpringRabbitMQComponent component = context.getComponent("spring-rabbitmq",
						SpringRabbitMQComponent.class);
				component.setConnectionFactory(connectionFactory);

				// scenario 1 - from bean to output
				from("direct:start").unmarshal()
					.json(JsonLibrary.Jackson, BookReturned.class)
					.bean(bookService)
					.marshal()
					.json(JsonLibrary.Jackson, BookReturned.class)
					.to("spring-rabbitmq:output?queues=output");
			}

		};
	}

}
