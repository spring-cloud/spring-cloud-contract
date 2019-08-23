/*
 * Copyright 2013-2019 the original author or authors.
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

package org.springframework.cloud.contract.verifier.config.framework;

import java.util.Arrays;
import java.util.List;

/**
 * Defines elements characteristic of JUnit5 test framework to be used during test class
 * construction.
 *
 * @author Olga Maciaszek-Sharma
 * @since 2.1.0
 */
public class JUnit5Definition implements TestFrameworkDefinition {

	@Override
	public String getClassModifier() {
		return "public ";
	}

	@Override
	public String getMethodModifier() {
		return "public void ";
	}

	@Override
	public String getLineSuffix() {
		return ";";
	}

	@Override
	public String getClassExtension() {
		return ".java";
	}

	@Override
	public String getClassNameSuffix() {
		return "Test";
	}

	@Override
	public String getIgnoreClass() {
		return "org.junit.jupiter.api.Disabled";
	}

	@Override
	public List<String> getOrderAnnotationImports() {
		return Arrays.asList("org.junit.jupiter.api.TestMethodOrder",
				"org.junit.jupiter.api.MethodOrderer");
	}

	@Override
	public String getOrderAnnotation() {
		return "@TestMethodOrder(MethodOrderer.Alphanumeric.class)";
	}

	@Override
	public String getIgnoreAnnotation() {
		return "@Disabled";
	}

	@Override
	public boolean annotationLevelRules() {
		return true;
	}

	@Override
	public String getRuleAnnotation(final String annotationValue) {
		return ("@ExtendWith(" + annotationValue + ".class)");
	}

}
