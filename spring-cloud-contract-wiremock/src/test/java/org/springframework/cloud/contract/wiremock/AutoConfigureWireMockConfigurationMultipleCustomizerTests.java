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

package org.springframework.cloud.contract.wiremock;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(
		classes = { WiremockTestsApplication.class,
				AutoConfigureWireMockConfigurationMultipleCustomizerTests.Config.class },
		webEnvironment = WebEnvironment.NONE)
@AutoConfigureWireMock(port = 0)
public class AutoConfigureWireMockConfigurationMultipleCustomizerTests {

	@Autowired
	private Config config;

	@Test
	public void bothCustomizersHaveBeenExecuted() throws Exception {
		assertThat(this.config.isExecuted()).isTrue();
	}

	@Configuration
	protected static class Config {

		boolean executedCustomizer1 = false;

		boolean executedCustomizer2 = false;

		@Bean
		WireMockConfigurationCustomizer optionsCustomizer1() {
			return new WireMockConfigurationCustomizer() {
				@Override
				public void customize(WireMockConfiguration options) {
					Config.this.executedCustomizer1 = true;
				}
			};
		}

		@Bean
		WireMockConfigurationCustomizer optionsCustomizer2() {
			return new WireMockConfigurationCustomizer() {
				@Override
				public void customize(WireMockConfiguration options) {
					Config.this.executedCustomizer2 = true;
				}
			};
		}

		public boolean isExecuted() {
			return this.executedCustomizer1 && this.executedCustomizer2;
		}

	}

}
