/*
 * Copyright 2013-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.contract.stubrunner;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.StringUtils;

/**
 * Reads property from system prop and from env var
 *
 * @author Marcin Grzejszczak
 * @since 2.0.0
 */
class StubRunnerPropertyUtils {

	private static final Log log = LogFactory.getLog(StubRunnerPropertyUtils.class);

	private static final String STUBRUNNER_PROPERTIES = "stubrunner.properties";

	static PropertyFetcher FETCHER = new PropertyFetcher();

	/**
	 * For Env vars takes the prop name, converts dots to underscores and applies
	 * upper case
	 */
	static boolean isPropertySet(String propName) {
		String value = getProperty(new HashMap<>(), propName);
		return StringUtils.hasText(value) && Boolean.parseBoolean(value);
	}

	/**
	 * Tries to pick a value from options, for Env vars takes the prop name, converts
	 * dots to underscores and applies upper case
	 */
	static String getProperty(Map<String, String> options, String propName) {
		if (options != null && options.containsKey(propName)) {
			String value = options.get(propName);
			if (log.isTraceEnabled()) {
				log.trace("Options map contains the prop [" + propName + "] with value [" + value + "]");
			}
			return value;
		}
		return doGetProp(appendPrefixIfNecessary(propName));
	}

	private static String appendPrefixIfNecessary(String prop) {
		if (prop.toLowerCase().startsWith("stubrunner")) {
			return prop;
		}
		return STUBRUNNER_PROPERTIES + "." + prop;
	}

	private static String doGetProp(String stubRunnerProp) {
		String systemProp = FETCHER.systemProp(stubRunnerProp);
		if (StringUtils.hasText(systemProp)) {
			if (log.isTraceEnabled()) {
				log.trace("System property [" + stubRunnerProp + "] has value [" + systemProp + "]");
			}
			return systemProp;
		}
		String convertedEnvProp = stubRunnerProp.replaceAll("\\.", "_")
				.replaceAll("-", "_").toUpperCase();
		String envVar = FETCHER.envVar(convertedEnvProp);
		if (log.isTraceEnabled()) {
			log.trace("Environment variable [" + convertedEnvProp + "] has value [" + envVar + "]");
		}
		return envVar;
	}
}

class PropertyFetcher {
	String systemProp(String prop) {
		return System.getProperty(prop);
	}
	String envVar(String prop) {
		return System.getenv(prop);
	}
}
