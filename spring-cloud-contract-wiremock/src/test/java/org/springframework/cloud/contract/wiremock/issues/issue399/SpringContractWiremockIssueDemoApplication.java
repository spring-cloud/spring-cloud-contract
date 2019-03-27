/*
 * Copyright 2013-2019 the original author or authors.
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

package org.springframework.cloud.contract.wiremock.issues.issue399;

import java.io.IOException;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.client.RestTemplateCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class SpringContractWiremockIssueDemoApplication {

	private static final int SOME_NOT_LOWEST_PRECEDENCE = Ordered.LOWEST_PRECEDENCE - 1;

	@Bean
	@Order(SOME_NOT_LOWEST_PRECEDENCE)
	@Profile("bug")
	public RestTemplateCustomizer someOrderedInterceptorCustomizer() {
		return new RestTemplateCustomizer() {
			@Override
			public void customize(RestTemplate restTemplate) {
				ClientHttpRequestInterceptor emptyInterceptor = new ClientHttpRequestInterceptor() {
					@Override
					public ClientHttpResponse intercept(HttpRequest request, byte[] body,
							ClientHttpRequestExecution execution) throws IOException {
						return execution.execute(request, body);
					}
				};
				restTemplate.getInterceptors().add(emptyInterceptor);
			}
		};
	}

	@Bean
	public RestTemplateCustomizer someNotOrderedInterceptorCustomizer() {
		return new RestTemplateCustomizer() {
			@Override
			public void customize(RestTemplate restTemplate) {
				ClientHttpRequestInterceptor emptyInterceptor = new ClientHttpRequestInterceptor() {
					@Override
					public ClientHttpResponse intercept(HttpRequest request, byte[] body,
							ClientHttpRequestExecution execution) throws IOException {
						return execution.execute(request, body);
					}
				};
				restTemplate.getInterceptors().add(emptyInterceptor);
			}
		};
	}

	@Bean
	@Order
	public RestTemplateCustomizer someLowestPrecedenceOrderedInterceptorCustomizer() {
		return new RestTemplateCustomizer() {
			@Override
			public void customize(RestTemplate restTemplate) {
				ClientHttpRequestInterceptor emptyInterceptor = new ClientHttpRequestInterceptor() {
					@Override
					public ClientHttpResponse intercept(HttpRequest request, byte[] body,
							ClientHttpRequestExecution execution) throws IOException {
						return execution.execute(request, body);
					}
				};
				restTemplate.getInterceptors().add(emptyInterceptor);
			}
		};
	}

}

class RestTemplateClient {

	private final RestTemplate restTemplate;

	RestTemplateClient(RestTemplateBuilder restTemplateBuilder) {
		this.restTemplate = restTemplateBuilder.build();
	}

	public String get() {
		return this.restTemplate.getForObject("/some-url", String.class);
	}

}
