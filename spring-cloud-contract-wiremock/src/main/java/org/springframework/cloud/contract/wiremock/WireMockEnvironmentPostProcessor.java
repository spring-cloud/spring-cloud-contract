/*
 * Copyright 2013-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.springframework.cloud.contract.wiremock;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

/**
 * Sets the web application type depending on presence of WebMvc / WebFlux API
 *
 * @author Marcin Grzejszczak
 * @since 2.0.3
 */
public class WireMockEnvironmentPostProcessor implements EnvironmentPostProcessor {
	private static final Log log = LogFactory
			.getLog(WireMockEnvironmentPostProcessor.class);

	private static final String SPRING_MAIN_WEB_APP_TYPE = "spring.main.web-application-type";
	private static final String PROPERTY_SOURCE_NAME = "defaultProperties";

	@Override
	public void postProcessEnvironment(ConfigurableEnvironment environment,
			SpringApplication application) {
		Map<String, Object> map = new HashMap<>();
		String webApplicationTypeFromProps = environment
				.getProperty(SPRING_MAIN_WEB_APP_TYPE);
		if (StringUtils.isEmpty(webApplicationTypeFromProps)) {
			WebApplicationType webApplicationType = application.getWebApplicationType();
			String webApplicationTypeName = WebApplicationType.NONE.name();
			if (WebApplicationType.NONE != webApplicationType) {
				webApplicationTypeName = isWebMvcApplication(application.getClassLoader()) ?
						WebApplicationType.SERVLET.name() : isReactiveApplication(application.getClassLoader()) ?
						WebApplicationType.REACTIVE.name() : WebApplicationType.NONE.name();
			}
			if (log.isDebugEnabled()) {
				log.debug("Setting [" + SPRING_MAIN_WEB_APP_TYPE + "] with value [" + webApplicationTypeName + "]");
			}
			map.put(SPRING_MAIN_WEB_APP_TYPE, webApplicationTypeName);
			addOrReplace(environment.getPropertySources(), map);
		}
		else if (log.isDebugEnabled()) {
			log.debug("Value of [" + SPRING_MAIN_WEB_APP_TYPE + "] already set to [" + webApplicationTypeFromProps + "]");
		}
	}

	private void addOrReplace(MutablePropertySources propertySources,
			Map<String, Object> map) {
		MapPropertySource target = null;
		if (propertySources.contains(PROPERTY_SOURCE_NAME)) {
			PropertySource<?> source = propertySources.get(PROPERTY_SOURCE_NAME);
			if (source instanceof MapPropertySource) {
				target = (MapPropertySource) source;
				for (String key : map.keySet()) {
					if (!target.containsProperty(key)) {
						target.getSource().put(key, map.get(key));
					}
				}
			}
		}
		if (target == null) {
			target = new MapPropertySource(PROPERTY_SOURCE_NAME, map);
		}
		if (!propertySources.contains(PROPERTY_SOURCE_NAME)) {
			propertySources.addLast(target);
		}
	}

	boolean isReactiveApplication(ClassLoader classLoader) {
		return ClassUtils.isPresent("org.springframework.web.reactive.DispatcherHandler",
				classLoader);
	}

	boolean isWebMvcApplication(ClassLoader classLoader) {
		return ClassUtils.isPresent("org.springframework.web.servlet.DispatcherServlet",
				classLoader);
	}

}
