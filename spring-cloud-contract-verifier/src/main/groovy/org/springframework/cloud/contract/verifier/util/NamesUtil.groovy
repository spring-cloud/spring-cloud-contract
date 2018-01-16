/*
 *  Copyright 2013-2017 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.springframework.cloud.contract.verifier.util

/**
 * A utility class that helps to convert names
 *
 * @author Jakub Kubrynski, codearte.io
 *
 * @since 1.0.0
 */
class NamesUtil {

	/**
	 * Returns the first element before the last separator presence.
	 * Returns empty string if separator is not found.
	 */
	static String beforeLast(String string, String separator) {
		if (string?.indexOf(separator) > -1) {
			return string.substring(0, string.lastIndexOf(separator))
		}
		return ''
	}

	/**
	 * Returns the first element after the last separator presence
	 * Returns the provided string if separator is not found.
	 */
	static String afterLast(String string, String separator) {
		if (string?.indexOf(separator) > -1) {
			return string.substring(string.lastIndexOf(separator) + 1)
		}
		return string
	}

	/**
	 * Returns the first element after the last dot presence
	 * Returns the provided string if separator is not found.
	 */
	static String afterLastDot(String string) {
		return afterLast(string, '.')
	}

	/**
	 * Converts a string into a camel case format
	 */
	static String camelCase(String className) {
		if (!className) {
			return className
		}
		String firstChar = className.charAt(0).toLowerCase() as String
		return firstChar + className.substring(1)
	}

	/**
	 * Capitalizes the provided string
	 */
	static String capitalize(String className) {
		if (!className) {
			return className
		}
		String firstChar = className.charAt(0).toUpperCase() as String
		return firstChar + className.substring(1)
	}

	/**
	 * Returns the whole string to the last present dot.
	 * Returns input string if there is no dot
	 */
	static String toLastDot(String string) {
		if (string?.indexOf('.') > -1) {
			return string.substring(0, string.lastIndexOf('.'))
		}
		return string
	}

	/**
	 * Converts the Java package notation to a path format
	 */
	static String packageToDirectory(String packageName) {
		return packageName.replace('.' as char, File.separatorChar)
	}

	/**
	 * Converts the path format to a Java package notation
	 */
	static String directoryToPackage(String directory) {
		return directory
				.replace('.', '_')
				.replace(File.separator, '.')
				.replaceAll('\\.([0-9])', '._$1')
	}

	/**
	 * Converts illegal package characters to underscores
	 */
	static String convertIllegalPackageChars(String packageName) {
		return packageName.replaceAll('[_\\- .+]', '_')
	}

	/**
	 * Converts illegal characters in method names to underscores
	 */
	static String convertIllegalMethodNameChars(String methodName) {
		String result =  methodName.replaceAll('^[^a-zA-Z_$0-9]', '_')
		return result.replaceAll('[^a-zA-Z_$0-9]', '_')
	}
}
