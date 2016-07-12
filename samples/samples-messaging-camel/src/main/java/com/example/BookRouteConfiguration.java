/*
 *  Copyright 2013-2016 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.example;

import org.apache.activemq.camel.component.ActiveMQComponent;
import org.apache.camel.RoutesBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.spring.SpringRouteBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Marcin Grzejszczak
 */
@Configuration
public class BookRouteConfiguration {

	@Bean
	ActiveMQComponent activeMQComponent(@Value("${activemq.url:vm://localhost?broker.persistent=false}") String url) {
		ActiveMQComponent component = new ActiveMQComponent();
		component.setBrokerURL(url);
		return component;
	}

	@Bean
	RoutesBuilder myRouter(final BookService bookService, final BookDeleter bookDeleter) {
		return new SpringRouteBuilder() {

			@Override
			public void configure() throws Exception {
				// scenario 1 - from bean to output
				from("direct:start").unmarshal().json(JsonLibrary.Jackson, BookReturned.class).bean(bookService).to("jms:output");
				// scenario 2 - from input to output
				from("jms:input").unmarshal().json(JsonLibrary.Jackson, BookReturned.class).bean(bookService).to("jms:output");
				// scenario 3 - from input to no output
				from("jms:delete").unmarshal().json(JsonLibrary.Jackson, BookDeleted.class).bean(bookDeleter);
			}

		};
	}
}
