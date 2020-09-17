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

import java.nio.file.Path;

/**
 * POJO that wraps data for a given generated method.
 *
 * @author Marcin Grzejszczak
 * @since 2.1.0
 */
public class GeneratedClassDataForMethod {

	private final SingleTestGenerator.GeneratedClassData generatedClassData;

	private final String methodName;

	public GeneratedClassDataForMethod(SingleTestGenerator.GeneratedClassData generatedClassData, String methodName) {
		this.generatedClassData = generatedClassData;
		this.methodName = methodName;
	}

	private SingleTestGenerator.GeneratedClassData assertClassData() {
		if (this.generatedClassData == null) {
			throw new IllegalStateException("No metadata was found for the generated test class");
		}
		return this.generatedClassData;
	}

	public String className() {
		return assertClassData().className;
	}

	public Path testClassPath() {
		return assertClassData().testClassPath;
	}

	public final SingleTestGenerator.GeneratedClassData getGeneratedClassData() {
		return generatedClassData;
	}

	public final String getMethodName() {
		return methodName;
	}

}
