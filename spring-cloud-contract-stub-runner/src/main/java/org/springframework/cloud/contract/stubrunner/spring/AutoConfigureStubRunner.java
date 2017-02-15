/*
 * Copyright 2012-2015 the original author or authors.
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

package org.springframework.cloud.contract.stubrunner.spring;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.properties.PropertyMapping;
import org.springframework.boot.test.autoconfigure.properties.SkipPropertyMapping;
import org.springframework.cloud.contract.verifier.messaging.boot.AutoConfigureMessageVerifier;

/**
 * @author Dave Syer
 *
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ImportAutoConfiguration
@AutoConfigureMessageVerifier
@PropertyMapping(value = "stubrunner", skip = SkipPropertyMapping.ON_DEFAULT_VALUE)
public @interface AutoConfigureStubRunner {

	/**
	 * Min value of a port for the automatically started WireMock server
	 */
	int minPort() default 10000;

	/**
	 * Max value of a port for the automatically started WireMock server
	 */
	int maxPort() default 15000;

	/**
	 * Should the stubs be checked for presence only locally
	 */
	boolean workOffline() default false;

	/**
	 * The repository root to use (where the stubs should be downloaded from)
	 */
	String repositoryRoot() default "";

	/**
	 * The ids of the stubs to run in "ivy" notation ([groupId]:artifactId:[classifier]:[version][:port]).
	 * {@code groupId}, {@code classifier}, {@code version} and {@code port} can be optional.
	 */
	String[] ids() default {};

	/**
	 * The classifier to use by default in ivy co-ordinates for a stub.
	 */
	String classifier() default "stubs";
}
