/*
 * Copyright 2012-2019 the original author or authors.
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

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.util.SocketUtils;

/**
 * Listener that prepares the environment so that WireMock will work when it is
 * initialized. For example, by finding free ports for the server to listen on.
 *
 * @author Dave Syer
 * @author Matt Garner
 *
 */
@Order(Ordered.LOWEST_PRECEDENCE)
public class WireMockApplicationListener
		implements ApplicationListener<ApplicationPreparedEvent> {

	@Override
	public void onApplicationEvent(ApplicationPreparedEvent event) {
		registerPort(event.getApplicationContext().getEnvironment());
	}

	private void registerPort(ConfigurableEnvironment environment) {
		Integer httpPortProperty = environment.getProperty("wiremock.server.port",
				Integer.class);
		// If the httpPortProperty is not found it means the AutoConfigureWireMock hasn't
		// been initialised.
		if (httpPortProperty == null) {
			return;
		}

		if (httpPortProperty.equals(0)) {
			MutablePropertySources propertySources = environment.getPropertySources();
			addPropertySource(propertySources);
			Map<String, Object> source = ((MapPropertySource) propertySources
					.get("wiremock")).getSource();
			source.put("wiremock.server.port",
					SocketUtils.findAvailableTcpPort(10000, 12500));
			source.put("wiremock.server.port-dynamic", true);
		}
		int httpsPortProperty = environment.getProperty("wiremock.server.https-port",
				Integer.class, 0);
		if (httpsPortProperty == 0) {
			MutablePropertySources propertySources = environment.getPropertySources();
			addPropertySource(propertySources);
			Map<String, Object> source = ((MapPropertySource) propertySources
					.get("wiremock")).getSource();
			source.put("wiremock.server.https-port",
					SocketUtils.findAvailableTcpPort(12500, 15000));
			source.put("wiremock.server.https-port-dynamic", true);
		}
		else if (httpsPortProperty == -1) {
			MutablePropertySources propertySources = environment.getPropertySources();
			addPropertySource(propertySources);
			Map<String, Object> source = ((MapPropertySource) propertySources
					.get("wiremock")).getSource();
			source.put("wiremock.server.https-port-dynamic", true);
		}

	}

	private void addPropertySource(MutablePropertySources propertySources) {
		if (!propertySources.contains("wiremock")) {
			propertySources.addFirst(
					new MapPropertySource("wiremock", new HashMap<String, Object>()));
		}
		else {
			// Move it up into first place
			PropertySource<?> wiremock = propertySources.remove("wiremock");
			propertySources.addFirst(wiremock);
		}
	}

}
