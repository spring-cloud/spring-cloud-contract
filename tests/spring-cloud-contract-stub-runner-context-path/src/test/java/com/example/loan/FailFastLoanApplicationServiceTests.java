/*
 *  Copyright 2013-2017 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *	   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.example.loan;

import wiremock.com.google.common.collect.ImmutableMap;

import org.junit.Test;
import org.springframework.beans.BeanInstantiationException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerConfiguration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

/**
 * @author Andrew Morgan
 */
public class FailFastLoanApplicationServiceTests {

	@Test
	public void shouldFailToStartContextWhenNoStubCanBeFound() {
		// When
		final Throwable throwable = catchThrowable(() -> new SpringApplicationBuilder(Application.class, StubRunnerConfiguration.class)
				.properties(ImmutableMap.of(
						"stubrunner.stubsMode", "REMOTE",
						"stubrunner.repositoryRoot", "classpath:m2repo/repository/",
						"stubrunner.ids", new String[]{"org.springframework.cloud.contract.verifier.stubs:should-not-be-found"}))
				.run());

		// Then
		assertThat(throwable).isInstanceOf(BeanCreationException.class);
		assertThat(throwable.getCause()).isInstanceOf(BeanInstantiationException.class);
		assertThat(throwable.getCause().getCause())
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("For groupId [org.springframework.cloud.contract.verifier.stubs] artifactId [should-not-be-found] "
						+ "and classifier [stubs] the version was not resolved! The following exceptions took place");
	}

	@Test
	public void shouldNotTryAndWorkOfflineWhenRemoteModeIsOn() {
		// When
		final Throwable throwable = catchThrowable(() -> new SpringApplicationBuilder(Application.class, StubRunnerConfiguration.class)
				.properties(ImmutableMap.of(
						"stubrunner.stubsMode", "CLASSPATH",
						"stubrunner.ids", new String[]{"org.springframework.cloud.contract.verifier.stubs:should-not-be-found"}))
				.run());

		// Then
		assertThat(throwable).isInstanceOf(BeanCreationException.class);
		assertThat(throwable.getCause()).isInstanceOf(BeanInstantiationException.class);
		assertThat(throwable.getCause().getCause())
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("No stubs were found on classpath ");
	}

}
