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
	 * The ids of the stubs to run in "ivy" notation ([groupId]:artifactId[:version][:classifier][:port]).
	 * {@code groupId}, {@code version}, {@code classifier} and {@code port} can be optional.
	 */
	String[] ids() default {};

	/**
	 * The classifier to use by default in ivy co-ordinates for a stub.
	 */
	String classifier() default "stubs";

	/**
	 * On the producer side the consumers can have a folder that contains contracts related only to them. By setting the flag to {@code true}
	 * we no longer register all stubs but only those that correspond to the consumer application's name. In other words
	 * we'll scan the path of every stub and if it contains the name of the consumer in the path only then will it get registered.
	 *
	 * Let's look at this example. Let's assume
	 * that we have a producer called {@code foo} and two consumers {@code baz} and {@code bar}. On the {@code foo} producer side the
	 * contracts would look like this
	 * {@code src/test/resources/contracts/baz-service/some/contracts/...} and
	 * {@code src/test/resources/contracts/bar-service/some/contracts/...}.
	 *
	 * Then when the consumer with {@code spring.application.name} or the {@link AutoConfigureStubRunner#consumerName()}
	 * annotation parameter set to {@code baz-service} will define the test setup as follows
	 * {@code @AutoConfigureStubRunner(ids = "com.example:foo:+:stubs:8095", stubsPerConsumer=true)} then only the stubs registered
	 * under {@code src/test/resources/contracts/baz-service/some/contracts/...} will get registered and those under
	 * {@code src/test/resources/contracts/bar-service/some/contracts/...} will get ignored.
	 *
	 * @see <a href="https://github.com/spring-cloud/spring-cloud-contract/issues/224">issue 224</a>
	 *
	 */
	boolean stubsPerConsumer() default false;

	/**
	 * You can override the default {@code spring.application.name} of this field by setting a value to this parameter.
	 *
	 * @see <a href="https://github.com/spring-cloud/spring-cloud-contract/issues/224">issue 224</a>
	 */
	String consumerName() default "";
}
