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

package org.springframework.cloud.contract.verifier.wiremock

import groovy.transform.CompileStatic

import org.springframework.cloud.contract.verifier.converter.StubGenerator
/**
 * WireMock implementation of the {@link StubGenerator}
 *
 * @since 1.0.0
 */
@CompileStatic
abstract class DslToWireMockConverter implements StubGenerator {

	@Override
	boolean canHandleFileName(String fileName) {
		return fileName.endsWith('.groovy')
	}

	@Override
	String generateOutputFileNameForInput(String inputFileName) {
		return inputFileName.replaceAll(extension(inputFileName), 'json')
	}

	private String extension(String inputFileName) {
		int i = inputFileName.lastIndexOf('.')
		if (i > 0) {
			return inputFileName.substring(i + 1)
		}
		return ""
	}
}
