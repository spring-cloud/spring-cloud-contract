/*
 * Copyright 2013-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.springframework.cloud.contract.wiremock;

import org.assertj.core.api.BDDAssertions;
import org.junit.Test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.mock.env.MockEnvironment;

public class WireMockEnvironmentPostProcessorTests {

	MockEnvironment environment = new MockEnvironment();
	SpringApplication springApplication = new SpringApplication();

	@Test
	public void should_pass_current_web_application_type() {
		WireMockEnvironmentPostProcessor processor = new WireMockEnvironmentPostProcessor();
		this.springApplication.setWebApplicationType(WebApplicationType.NONE);

		processor.postProcessEnvironment(this.environment, this.springApplication);

		BDDAssertions.then(this.environment.getProperty("spring.main.web-application-type"))
				.isEqualToIgnoringCase("none");
	}

	@Test
	public void should_pass_current_web_application_type_from_properties() {
		WireMockEnvironmentPostProcessor processor = new WireMockEnvironmentPostProcessor();
		this.environment.setProperty("spring.main.web-application-type", "none");

		processor.postProcessEnvironment(this.environment, this.springApplication);

		BDDAssertions.then(this.environment.getProperty("spring.main.web-application-type"))
				.isEqualToIgnoringCase("none");
	}

	@Test
	public void should_set_reactive_when_reactive_class_is_found() {
		WireMockEnvironmentPostProcessor processor = new WireMockEnvironmentPostProcessor() {
			@Override
			boolean isWebMvcApplication(ClassLoader classLoader) {
				return false;
			}

			@Override
			boolean isReactiveApplication(ClassLoader classLoader) {
				return true;
			}
		};
		this.springApplication.setWebApplicationType(WebApplicationType.SERVLET);

		processor.postProcessEnvironment(this.environment, this.springApplication);

		BDDAssertions.then(this.environment.getProperty("spring.main.web-application-type"))
				.isEqualToIgnoringCase("REACTIVE");
	}

	@Test
	public void should_set_servlet_when_reactive_class_is_not_found_and_servlet_is_found() {
		WireMockEnvironmentPostProcessor processor = new WireMockEnvironmentPostProcessor() {
			@Override
			boolean isWebMvcApplication(ClassLoader classLoader) {
				return true;
			}

			@Override
			boolean isReactiveApplication(ClassLoader classLoader) {
				return false;
			}
		};
		this.springApplication.setWebApplicationType(WebApplicationType.REACTIVE);

		processor.postProcessEnvironment(this.environment, this.springApplication);

		BDDAssertions.then(this.environment.getProperty("spring.main.web-application-type"))
				.isEqualToIgnoringCase("SERVLET");
	}

	@Test
	public void should_set_none_when_reactive_and_servlet_classes_are_not_found() {
		WireMockEnvironmentPostProcessor processor = new WireMockEnvironmentPostProcessor() {

			@Override
			boolean isWebMvcApplication(ClassLoader classLoader) {
				return false;
			}

			@Override
			boolean isReactiveApplication(ClassLoader classLoader) {
				return false;
			}
		};
		this.springApplication.setWebApplicationType(WebApplicationType.NONE);

		processor.postProcessEnvironment(this.environment, this.springApplication);

		BDDAssertions.then(this.environment.getProperty("spring.main.web-application-type"))
				.isEqualToIgnoringCase("NONE");
	}

}