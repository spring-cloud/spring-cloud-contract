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

import org.springframework.util.StringUtils;

/**
 * Reads property from system prop and from env var
 *
 * @author Marcin Grzejszczak
 * @since 2.0.0
 */
class PropertyUtil {

	static PropertyFetcher FETCHER = new PropertyFetcher();

	/**
	 * For Env vars takes the prop name, converts dots to underscores and applies upper case
	 */
	static boolean isPropertySet(String propName) {
		String skipSnapCheckProp = FETCHER.systemProp(propName);
		String envProp = propName.replaceAll("\\.", "_")
				.replaceAll("-", "_").toUpperCase();
		String skipSnapCheckEnv = FETCHER.envVar(envProp);
		if (StringUtils.hasText(skipSnapCheckProp)) {
			return Boolean.parseBoolean(skipSnapCheckProp);
		}
		return StringUtils.hasText(skipSnapCheckEnv) && Boolean
				.parseBoolean(skipSnapCheckEnv);
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
