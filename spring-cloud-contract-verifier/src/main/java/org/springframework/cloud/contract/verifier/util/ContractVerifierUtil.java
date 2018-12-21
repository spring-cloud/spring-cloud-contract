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

package org.springframework.cloud.contract.verifier.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Helper class for the generated tests
 *
 * @author Marcin Grzejszczak
 * @since 2.1.0
 */
public class ContractVerifierUtil {

	/**
	 * Helper method to convert a file to bytes
	 *
	 * @param testClass - test class relative to which the file is stored
	 * @param relativePath - relative path to the file
	 * @return bytes of the file
	 */
	public static byte[] fileToBytes(Object testClass, String relativePath) {
		try {
			URL url = testClass.getClass().getResource(relativePath);
			if (url == null) {
				throw new FileNotFoundException(relativePath);
			}
			return Files.readAllBytes(Paths.get(url.toURI()));
		}
		catch (IOException | URISyntaxException ex) {
			throw new IllegalStateException(ex);
		}
	}

}
