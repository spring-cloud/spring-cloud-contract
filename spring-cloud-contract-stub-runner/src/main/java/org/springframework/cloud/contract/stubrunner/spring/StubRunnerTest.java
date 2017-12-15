/*
 * Copyright 2017 the original author or authors.
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

import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTestContextBootstrapper;
import org.springframework.core.annotation.AliasFor;
import org.springframework.test.context.BootstrapWith;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation representing a slice test for Stub runner. It loads up auto-configurations
 * related to running of the stubs
 *
 * @author Biju Kunjummen
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@BootstrapWith(SpringBootTestContextBootstrapper.class)
@OverrideAutoConfiguration(enabled = false)
@AutoConfigureStubRunner
@ImportAutoConfiguration
public @interface StubRunnerTest {

	@AliasFor(annotation = AutoConfigureStubRunner.class, attribute = "minPort")
	int minPort() default 10000;

	@AliasFor(annotation = AutoConfigureStubRunner.class, attribute = "maxPort")
	int maxPort() default 15000;

	@AliasFor(annotation = AutoConfigureStubRunner.class, attribute = "workOffline")
	boolean workOffline() default false;

	@AliasFor(annotation = AutoConfigureStubRunner.class, attribute = "repositoryRoot")
	String repositoryRoot() default "";

	@AliasFor(annotation = AutoConfigureStubRunner.class, attribute = "ids")
	String[] ids() default {};

	@AliasFor(annotation = AutoConfigureStubRunner.class, attribute = "classifier")
	String classifier() default "stubs";

	@AliasFor(annotation = AutoConfigureStubRunner.class, attribute = "stubsPerConsumer")
	boolean stubsPerConsumer() default false;

	@AliasFor(annotation = AutoConfigureStubRunner.class, attribute = "consumerName")
	String consumerName() default "";

	@AliasFor(annotation = AutoConfigureStubRunner.class, attribute = "mappingsOutputFolder")
	String mappingsOutputFolder() default "";
}
