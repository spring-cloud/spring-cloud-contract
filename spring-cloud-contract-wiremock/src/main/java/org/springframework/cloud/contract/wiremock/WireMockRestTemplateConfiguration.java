/*
 * Copyright 2016-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.contract.wiremock;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.web.client.RestTemplateCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * @author Dave Syer
 *
 */
@Configuration
public class WireMockRestTemplateConfiguration {

	@Bean
	@ConditionalOnClass(SSLContextBuilder.class)
	public RestTemplateCustomizer restTemplateCustomizer() {
		return new RestTemplateCustomizer() {
			@Override
			public void customize(RestTemplate restTemplate) {
				HttpComponentsClientHttpRequestFactory factory = (HttpComponentsClientHttpRequestFactory) restTemplate
						.getRequestFactory();
				factory.setHttpClient(createSslHttpClient());
			}

			private HttpClient createSslHttpClient() {
				try {
					SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(
							new SSLContextBuilder().loadTrustMaterial(null,
									TrustSelfSignedStrategy.INSTANCE).build(),
							NoopHostnameVerifier.INSTANCE);
					return HttpClients.custom().setSSLSocketFactory(socketFactory)
							.build();
				}
				catch (Exception ex) {
					throw new IllegalStateException("Unable to create SSL HttpClient",
							ex);
				}
			}
		};
	}
	
}
