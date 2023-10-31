/*
 * Copyright 2012-2020 the original author or authors.
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

import java.util.Optional;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.cloud.test.TestSocketUtils;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;

/**
 * Listener that prepares the environment so that WireMock will work when it is
 * initialized. For example, by finding free ports for the server to listen on.
 *
 * @author Dave Syer
 * @author Matt Garner
 *
 */
@Order(Ordered.LOWEST_PRECEDENCE)
public class WireMockApplicationListener implements ApplicationListener<ApplicationPreparedEvent> {

	private static final Log log = LogFactory.getLog(WireMockApplicationListener.class);

	@Override
	public void onApplicationEvent(ApplicationPreparedEvent event) {
		registerPort(event.getApplicationContext().getEnvironment());
	}

	private void registerPort(ConfigurableEnvironment environment) {
		Integer httpPortProperty = environment.getProperty("wiremock.server.port", Integer.class);
		// If the httpPortProperty is not found it means the AutoConfigureWireMock hasn't
		// been initialised or the default was used
		if (httpPortProperty != null && isHttpDynamic(httpPortProperty)) {
			registerPropertySourceForDynamicEntries(environment, "wiremock.server.port", 10000, 12500,
					"wiremock.server.port-dynamic");
			if (log.isDebugEnabled()) {
				log.debug("Registered property source for dynamic http port");
			}
		}
		else {
			Map<String, Object> source = getWireMockSource(environment);
			Integer port = Optional.ofNullable(httpPortProperty).orElse(8080);
			source.put("wiremock.server.port", port);
			if (log.isDebugEnabled()) {
				log.debug("Registered WireMock server port property to the <" + port + "> value");
			}
		}
		int httpsPortProperty = environment.getProperty("wiremock.server.https-port", Integer.class, 0);
		if (isHttpsDynamic(httpsPortProperty)) {
			registerPropertySourceForDynamicEntries(environment, "wiremock.server.https-port", 12500, 15000,
					"wiremock.server.https-port-dynamic");
			if (log.isDebugEnabled()) {
				log.debug("Registered property source for dynamic https port");
			}
		}
		else if (httpsPortProperty == -1) {
			Map<String, Object> source = getWireMockSource(environment);
			source.put("wiremock.server.https-port-dynamic", true);
			if (log.isDebugEnabled()) {
				log.debug("Registered property source for dynamic https with https port property set to -1");
			}
		}

	}

	private Map<String, Object> getWireMockSource(ConfigurableEnvironment environment) {
		MutablePropertySources propertySources = environment.getPropertySources();
		addPropertySource(propertySources);
		Map<String, Object> source = ((MapPropertySource) propertySources.get("wiremock")).getSource();
		return source;
	}

	private boolean isHttpsDynamic(int httpsPortProperty) {
		return httpsPortProperty == 0;
	}

	private boolean isHttpDynamic(Integer httpPortProperty) {
		return httpPortProperty.equals(0);
	}

	private void registerPropertySourceForDynamicEntries(ConfigurableEnvironment environment, String portProperty,
			int minPort, int maxPort, String dynamicPortProperty) {
		Map<String, Object> source = getWireMockSource(environment);
		int port = TestSocketUtils.findAvailableTcpPort(minPort, maxPort);
		source.put(portProperty, port);
		if (log.isDebugEnabled()) {
			log.debug("Registered property source for property [" + portProperty + "] with value [" + port + "]");
		}
		source.put(dynamicPortProperty, true);
	}

	private void addPropertySource(MutablePropertySources propertySources) {
		if (!propertySources.contains("wiremock")) {
			propertySources.addFirst(new MapPropertySource("wiremock", new HashMap<String, Object>()));
		}
		else {
			// Move it up into first place
			PropertySource<?> wiremock = propertySources.remove("wiremock");
			propertySources.addFirst(wiremock);
		}
	}

}
