/*
 * Copyright 2013-2020 the original author or authors.
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

package org.springframework.cloud.contract.verifier.builder;

import java.io.File;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.cloud.contract.verifier.util.NamesUtil;
import org.springframework.util.StringUtils;

class BaseClassProvider {

	private static final Log log = LogFactory.getLog(BaseClassProvider.class);

	private static final String SEPARATOR = "_REPLACEME_";

	String retrieveBaseClass(Map<String, String> baseClassMappings,
			String packageWithBaseClasses, String baseClassForTests,
			String includedDirectoryRelativePath) {
		String contractPathAsPackage = includedDirectoryRelativePath
				.replace(File.separator, ".");
		String contractPackage = includedDirectoryRelativePath.replace(File.separator,
				SEPARATOR);
		// package mapping takes super precedence
		if (baseClassMappings != null && !baseClassMappings.isEmpty()) {
			Optional<Map.Entry<String, String>> mapping = baseClassMappings.entrySet()
					.stream().filter(entry -> {
						String pattern = entry.getKey();
						return contractPathAsPackage.matches(pattern);
					}).findFirst();
			if (log.isDebugEnabled()) {
				log.debug("Matching pattern for contract package ["
						+ contractPathAsPackage + "] with setup " + baseClassMappings
						+ " is [" + mapping + "]");
			}
			if (mapping.isPresent()) {
				return mapping.get().getValue();
			}
		}
		if (StringUtils.isEmpty(packageWithBaseClasses)) {
			return baseClassForTests;
		}
		String generatedClassName = generateDefaultBaseClassName(contractPackage,
				packageWithBaseClasses);
		return generatedClassName + "Base";
	}

	private String generateDefaultBaseClassName(String classPackage,
			String packageWithBaseClasses) {
		String[] splitPackage = NamesUtil.convertIllegalPackageChars(classPackage)
				.split(SEPARATOR);
		if (splitPackage.length > 1) {
			String last = NamesUtil.capitalize(splitPackage[splitPackage.length - 1]);
			String butLast = NamesUtil.capitalize(splitPackage[splitPackage.length - 2]);
			return packageWithBaseClasses + "." + butLast + last;
		}
		return packageWithBaseClasses + "." + NamesUtil.capitalize(splitPackage[0]);
	}

}
