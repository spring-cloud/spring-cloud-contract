/*
 *  Copyright 2013-2016 the original author or authors.
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

package org.springframework.cloud.contract.verifier.converter

import groovy.transform.CompileStatic
import org.springframework.cloud.contract.verifier.file.ContractMetadata

/**
 * Converts contracts into their stub representation
 *
 * @since 1.0.0
 */
@CompileStatic
interface SingleFileConverter {

	/**
	 * Returns {@code true} if the converter can handle the file.
	 */
	boolean canHandleFileName(String fileName)

	/**
	 * Returns the content of the converted file
	 */
	String convertContent(String rootName, ContractMetadata content)

	/**
	 * Returns the name of the converted file
	 */
	String generateOutputFileNameForInput(String inputFileName)
}