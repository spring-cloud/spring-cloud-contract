/*
 * Copyright 2013-2020 the original author or authors.
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
import org.apache.camel.component.rabbitmq.RabbitMQComponent;
import org.apache.camel.model.dataformat.JsonLibrary;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Marcin Grzejszczak
 */
@Configuration(proxyBeanMethods = false)
public class BookRouteConfiguration {

	@Bean
	RoutesBuilder myRouter(final BookService bookService, final BookDeleter bookDeleter, CamelContext context,
			@Value("${spring.rabbitmq.port}") int port) {
		return new RouteBuilder() {

			@Override
			public void configure() throws Exception {
				RabbitMQComponent component = context.getComponent("rabbitmq", RabbitMQComponent.class);
				component.setAddresses("localhost:" + port);

				// scenario 1 - from bean to output
				from("direct:start").unmarshal().json(JsonLibrary.Jackson, BookReturned.class).bean(bookService)
						.marshal().json(JsonLibrary.Jackson, BookReturned.class).to("rabbitmq:output?queue=output");
				// scenario 2 - from input to output
				from("rabbitmq:input?queue=input").unmarshal().json(JsonLibrary.Jackson, BookReturned.class)
						.bean(bookService).marshal().json(JsonLibrary.Jackson, BookReturned.class)
						.to("rabbitmq:output");
				// scenario 3 - from input to no output
				from("rabbitmq:delete?queue=delete").unmarshal().json(JsonLibrary.Jackson, BookDeleted.class)
						.bean(bookDeleter);
			}

		};
	}

}
