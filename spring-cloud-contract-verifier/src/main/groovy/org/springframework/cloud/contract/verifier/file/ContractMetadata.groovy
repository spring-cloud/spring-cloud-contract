/*
 *  Copyright 2013-2016 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License")
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

package org.springframework.cloud.contract.verifier.file

import groovy.transform.CompileStatic
import groovy.transform.ToString
import org.springframework.cloud.contract.spec.Contract

import java.nio.file.Path
/**
 * Contains metadata for a particular file with a DSL
 *
 * @author Jakub Kubrynski, codearte.io
 *
 * @since 1.0.0
 */
@CompileStatic
@ToString
class ContractMetadata {
	/**
	 * Path to the file
	 */
	final Path path
	/**
	 * Should the contract be ignored
	 */
	final boolean ignored
	/**
	 * How many files are there in the folder
	 */
	final int groupSize
	/**
	 * If scenario related will
	 */
	final Integer order
	/**
	 * When we have already converted a contract via a converter this
	 * field will be set
	 */
	final Contract convertedContract

	ContractMetadata(Path path, boolean ignored, int groupSize, Integer order, Contract convertedContract = null) {
		this.groupSize = groupSize
		this.path = path
		this.ignored = ignored
		this.order = order
		this.convertedContract = convertedContract
	}
}
