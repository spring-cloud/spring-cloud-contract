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

package org.springframework.cloud.contract.verifier.config;

import org.springframework.cloud.contract.verifier.config.framework.CustomDefinition;
import org.springframework.cloud.contract.verifier.config.framework.JUnit5Definition;
import org.springframework.cloud.contract.verifier.config.framework.JUnitDefinition;
import org.springframework.cloud.contract.verifier.config.framework.SpockDefinition;
import org.springframework.cloud.contract.verifier.config.framework.TestFrameworkDefinition;
import org.springframework.cloud.contract.verifier.config.framework.TestNGDefinition;

/**
 * Contains main building blocks for a test class for the given framework.
 *
 * @author Jakub Kubrynski, codearte.io
 * @author Olga Maciaszek-Sharma
 *
 * @since 1.0.0
 */
public enum TestFramework {

	/**
	 * Spock test framework.
	 */
	SPOCK(new SpockDefinition()),
	/**
	 * JUnit test framework.
	 */
	JUNIT(new JUnitDefinition()),
	/**
	 * JUnit5 test framework.
	 */
	JUNIT5(new JUnit5Definition()),
	/**
	 * TestNG test framework.
	 */
	TESTNG(new TestNGDefinition()),
	/**
	 * Custom test framework.
	 */
	CUSTOM(new CustomDefinition());

	private final TestFrameworkDefinition testFrameworkDefinition;

	TestFramework(TestFrameworkDefinition testFrameworkDefinition) {
		this.testFrameworkDefinition = testFrameworkDefinition;
	}

	public String getClassExtension() {
		return this.testFrameworkDefinition.getClassExtension();
	}

	public String getClassNameSuffix() {
		return this.testFrameworkDefinition.getClassNameSuffix();
	}

	public String getIgnoreClass() {
		return this.testFrameworkDefinition.getIgnoreClass();
	}

	public String getIgnoreAnnotation() {
		return this.testFrameworkDefinition.getIgnoreAnnotation();
	}

}
