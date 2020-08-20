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

import java.util.List;

/**
 * Defines elements characteristic of TestNG test framework to be used during test class
 * construction.
 *
 * @author André Hoffmann
 * @since 2.2.0
 */
public class TestNGDefinition implements TestFrameworkDefinition {

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
		throw new UnsupportedOperationException(
				"There is no @Ignore annotation for TestNG. A test can be disabled directly in the @Test annotation");
	}

	@Override
	public List<String> getOrderAnnotationImports() {
		throw new UnsupportedOperationException("Not implemented yet in TestNG");
	}

	@Override
	public String getOrderAnnotation() {
		throw new UnsupportedOperationException("Not implemented yet in TestNG");
	}

	@Override
	public String getIgnoreAnnotation() {
		throw new UnsupportedOperationException(
				"There is no @Ignore annotation for TestNG. A test can be disabled directly in the @Test annotation");
	}

	@Override
	public boolean annotationLevelRules() {
		return false;
	}

	@Override
	public String getRuleAnnotation(String annotationValue) {
		throw new UnsupportedOperationException("Not available in TestNG.");
	}

}
