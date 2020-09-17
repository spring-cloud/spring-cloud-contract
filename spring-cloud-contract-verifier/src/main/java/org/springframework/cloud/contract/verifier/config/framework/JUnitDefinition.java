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

package org.springframework.cloud.contract.verifier.config.framework;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Defines elements characteristic of JUnit test framework to be used during test class
 * construction.
 *
 * @author Olga Maciaszek-Sharma
 * @since 2.1.0
 */
public class JUnitDefinition implements TestFrameworkDefinition {

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
		return "org.junit.Ignore";
	}

	@Override
	public List<String> getOrderAnnotationImports() {
		return new ArrayList<String>(Arrays.asList("org.junit.FixMethodOrder", "org.junit.runners.MethodSorters"));
	}

	@Override
	public String getOrderAnnotation() {
		return "@FixMethodOrder(MethodSorters.NAME_ASCENDING)";
	}

	@Override
	public String getIgnoreAnnotation() {
		return "@Ignore";
	}

	@Override
	public boolean annotationLevelRules() {
		return false;
	}

	@Override
	public String getRuleAnnotation(String annotationValue) {
		throw new UnsupportedOperationException("Not available in JUnit.");
	}

}
