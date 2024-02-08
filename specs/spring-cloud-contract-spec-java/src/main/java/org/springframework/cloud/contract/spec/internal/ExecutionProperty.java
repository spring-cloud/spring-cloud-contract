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

package org.springframework.cloud.contract.spec.internal;

import java.io.Serializable;
import java.util.Objects;

/**
 * Represents a property that will become an executable method in the generated tests.
 *
 * @since 1.0.0
 */
public class ExecutionProperty implements Serializable {

	private static final String PLACEHOLDER_VALUE = "$it";

	private final String executionCommand;

	public ExecutionProperty(String executionCommand) {
		this.executionCommand = executionCommand;
	}

	/**
	 * Inserts the provided code as a parameter to the method and returns the code that
	 * represents that method execution.
	 * @param valueToInsert value to insert into the string
	 * @return string with inserted value
	 */
	public String insertValue(String valueToInsert) {
		return executionCommand.replace(PLACEHOLDER_VALUE, valueToInsert);
	}

	@Override
	public String toString() {
		return executionCommand;
	}

	public final String getExecutionCommand() {
		return executionCommand;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		ExecutionProperty that = (ExecutionProperty) o;
		return Objects.equals(executionCommand, that.executionCommand);
	}

	@Override
	public int hashCode() {
		return Objects.hash(executionCommand);
	}

}
