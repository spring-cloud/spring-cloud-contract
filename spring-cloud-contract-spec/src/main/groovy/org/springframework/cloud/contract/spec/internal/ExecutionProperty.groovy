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

package org.springframework.cloud.contract.spec.internal

import groovy.transform.CompileStatic

/**
 * Represents a property that will become an executable method in the
 * generated tests
 *
 * @since 1.0.0
 */
@CompileStatic
class ExecutionProperty {

	private static final String PLACEHOLDER_VALUE = '\\$it'

	final String executionCommand

	ExecutionProperty(String executionCommand) {
		this.executionCommand = executionCommand
	}

	/**
	 * Inserts the provided code as a parameter to the method and returns
	 * the code that represents that method execution
	 */
	String insertValue(String valueToInsert) {
		return executionCommand.replaceAll(PLACEHOLDER_VALUE, valueToInsert)
	}
}
