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
	 * @return the class access level modifier. E.g. for Java tests that would mean
	 * {@code public}
	 */
	String getClassModifier();

	/**
	 * @return the method access level modifier along with the return type. E.g. for Java
	 * tests that would mean {@code public void}
	 */
	String getMethodModifier();

	/**
	 * @return the characters that should end each line. E.g. for Java tests that would
	 * mean {@code ;}
	 */
	String getLineSuffix();

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
	 * @return the qualified names of the classes that are used for arranging tests into
	 * scenarios. E.g. for JUnit 4 tests that would be {@code 'org.junit.FixMethodOrder'},
	 * {@code 'org.junit.runners.MethodSorters'}
	 */
	List<String> getOrderAnnotationImports();

	/**
	 * @return the annotation used for arranging tests into scenarios. E.g. for JUnit test
	 * that would be {@code @FixMethodOrder(MethodSorters.NAME_ASCENDING)}
	 */
	String getOrderAnnotation();

	/**
	 * @return the annotation used for ignoring or disabling tests. E.g. for JUnit tests
	 * that would mean {@code @Ignore}
	 */
	String getIgnoreAnnotation();

	/**
	 * @return a boolean indicating whether an annotation-type rule or extension is being
	 * used or not. E.g. for JUnit 5 tests that would return {@code true}
	 */
	boolean annotationLevelRules();

	/**
	 * @param annotationValue value of the annotation
	 * @return the test rule or extension annotation with the annotationValue passed as
	 * an argument. E.g. for JUnit 5 tests that could be
	 * {@code @ExtendWith(Example.class)}
	 */
	String getRuleAnnotation(String annotationValue);

}
