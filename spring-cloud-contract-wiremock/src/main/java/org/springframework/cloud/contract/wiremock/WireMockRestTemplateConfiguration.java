/*
 * Copyright 2016-2020 the original author or authors.
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

package org.springframework.cloud.contract.wiremock;

import java.lang.reflect.Field;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactoryBuilder;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.ssl.SSLContextBuilder;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.client.RestTemplateCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInitializer;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.InterceptingClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.util.ReflectionUtils;

import static org.apache.hc.client5.http.ssl.NoopHostnameVerifier.INSTANCE;

/**
 * @author Dave Syer
 * @author Nikola Kološnjaji
 *
 */
@Configuration(proxyBeanMethods = false)
public class WireMockRestTemplateConfiguration {

	@Bean
	@ConditionalOnClass(SSLContextBuilder.class)
	@ConditionalOnProperty(value = "wiremock.rest-template-ssl-enabled", matchIfMissing = true)
	public RestTemplateCustomizer wiremockRestTemplateCustomizer() {
		return new RestTemplateCustomizer() {
			@Override
			public void customize(RestTemplate restTemplate) {
				if (restTemplate.getRequestFactory() instanceof HttpComponentsClientHttpRequestFactory factory) {
					factory.setHttpClient(createSslHttpClient());
				}
				else if (restTemplate.getRequestFactory() instanceof InterceptingClientHttpRequestFactory) {
					Field requestFactoryField = ReflectionUtils.findField(RestTemplate.class, "requestFactory");
					if (requestFactoryField != null) {
						requestFactoryField.setAccessible(true);
						ClientHttpRequestFactory requestFactory = (ClientHttpRequestFactory) ReflectionUtils.getField(requestFactoryField, restTemplate);
						if (requestFactory instanceof HttpComponentsClientHttpRequestFactory factory) {
							factory.setHttpClient(createSslHttpClient());
						}
					}
				}
			}

			private HttpClient createSslHttpClient() {
				try {

					SSLConnectionSocketFactoryBuilder sslConnectionSocketFactoryBuilder = SSLConnectionSocketFactoryBuilder
							.create();
					sslConnectionSocketFactoryBuilder
							.setSslContext(new SSLContextBuilder()
									.loadTrustMaterial(null, TrustSelfSignedStrategy.INSTANCE).build())
							.setHostnameVerifier(INSTANCE);
					PoolingHttpClientConnectionManager connectionManager = PoolingHttpClientConnectionManagerBuilder
							.create().setSSLSocketFactory(sslConnectionSocketFactoryBuilder.build()).build();
					return HttpClients.custom().setConnectionManager(connectionManager).build();
				}
				catch (Exception ex) {
					throw new IllegalStateException("Unable to create SSL HttpClient", ex);
				}
			}
		};
	}

}
