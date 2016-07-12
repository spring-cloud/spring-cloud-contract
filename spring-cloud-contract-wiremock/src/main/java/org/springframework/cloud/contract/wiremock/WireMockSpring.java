/*
 * Copyright 2015 the original author or authors.
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

import javax.net.ssl.HttpsURLConnection;

import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.ssl.SSLContexts;
import org.junit.Assert;
import org.springframework.util.ClassUtils;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

/**
 * Convenience factory class for a {@link WireMockConfiguration} that knows how to use
 * Spring Boot to create a stub server. Use, for example, in a JUnit rule:
 * 
 * <pre>
 * &#64;ClassRule
 * public static WireMockClassRule wiremock = new WireMockClassRule(
 * 		WireMockSpring.config());
 * </pre>
 * 
 * and then use {@link WireMock} as normal in your test methods.
 * 
 * @author Dave Syer
 *
 */
public abstract class WireMockSpring {

	private static boolean initialized = false;

	public static WireMockConfiguration config() {
		if (!initialized) {
			if (ClassUtils.isPresent("org.apache.http.conn.ssl.NoopHostnameVerifier",
					null)) {
				HttpsURLConnection
						.setDefaultHostnameVerifier(NoopHostnameVerifier.INSTANCE);
				try {
					HttpsURLConnection
							.setDefaultSSLSocketFactory(SSLContexts.custom()
									.loadTrustMaterial(null,
											TrustSelfSignedStrategy.INSTANCE)
									.build().getSocketFactory());
				}
				catch (Exception e) {
					Assert.fail("Cannot install custom socket factory: [" + e.getMessage()
							+ "]");
				}
			}
			initialized = true;
		}
		WireMockConfiguration config = new WireMockConfiguration();
		config.httpServerFactory(new SpringBootHttpServerFactory());
		return config;
	}

}
