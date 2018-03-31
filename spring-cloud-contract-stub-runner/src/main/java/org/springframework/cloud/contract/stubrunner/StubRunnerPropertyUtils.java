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

import org.springframework.util.StringUtils;

/**
 * Reads property from system prop and from env var
 *
 * @author Marcin Grzejszczak
 * @since 2.0.0
 */
class StubRunnerPropertyUtils {

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
			return options.get(propName);
		}
		String directTry = doGetProp(propName);
		if (StringUtils.hasText(directTry)) {
			return directTry;
		}
		return doGetProp(STUBRUNNER_PROPERTIES + "." + propName);
	}

	private static String doGetProp(String stubRunnerProp) {
		String systemProp = FETCHER.systemProp(stubRunnerProp);
		if (StringUtils.hasText(systemProp)) {
			return systemProp;
		}
		String convertedEnvProp = stubRunnerProp.replaceAll("\\.", "_")
				.replaceAll("-", "_").toUpperCase();
		return FETCHER.envVar(convertedEnvProp);
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
