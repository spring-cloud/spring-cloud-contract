/*
 * Copyright 2013-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.contract.verifier.builder;

/**
 * @author Marcin Grzejszczak
 * @since
 */
class GeneratedClassDataForMethod {
	final SingleTestGenerator.GeneratedClassData generatedClassData;
	final String methodName;

	GeneratedClassDataForMethod(SingleTestGenerator.GeneratedClassData generatedClassData,
			String methodName) {
		this.generatedClassData = generatedClassData;
		this.methodName = methodName;
	}

	private SingleTestGenerator.GeneratedClassData assertClassData() {
		if (this.generatedClassData == null) {
			throw new IllegalStateException("No metadata was found for the generated test class");
		}
		return this.generatedClassData;
	}

	String className() {
		return assertClassData().className;
	}

	java.nio.file.Path testClassPath() {
		return assertClassData().testClassPath;
	}
}
