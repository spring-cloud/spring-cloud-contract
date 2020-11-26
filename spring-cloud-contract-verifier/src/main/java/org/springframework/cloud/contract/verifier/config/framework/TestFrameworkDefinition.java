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

/**
 * @deprecated appropriate implementations of
 * <code>org.springframework.cloud.contract.verifier.builder.Visitor</code> should be used
 * instead.
 *
 * Defines elements characteristic of a given test framework to be used during test class
 * construction.
 * @author Olga Maciaszek-Sharma
 * @since 2.1.0
 */
@Deprecated
public interface TestFrameworkDefinition {

	/**
	 * @return the file extension. E.g. for Java tests that would be {@code .java}
	 */
	String getClassExtension();

	/**
	 * @return the test class name suffix. E.g. for JUnit tests that would be {@code Test}
	 */
	String getClassNameSuffix();

	/**
	 * @return the qualified name of the class used to ignore or disable tests. E.g. for
	 * JUnit 4 tests that would be {@code org.junit.Ignore}
	 */
	String getIgnoreClass();

	/**
	 * @return the annotation used for ignoring or disabling tests. E.g. for JUnit tests
	 * that would mean {@code @Ignore}
	 */
	String getIgnoreAnnotation();

}
