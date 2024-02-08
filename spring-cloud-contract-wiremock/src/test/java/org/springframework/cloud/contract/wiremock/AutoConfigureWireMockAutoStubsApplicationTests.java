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

import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = WiremockTestsApplication.class,
		properties = "app.baseUrl=http://localhost:${wiremock.server.port}", webEnvironment = WebEnvironment.NONE)
@AutoConfigureWireMock(port = 0)
// Default stubs work at classpath:/mappings
public class AutoConfigureWireMockAutoStubsApplicationTests {

	@Autowired
	private Service service;

	@Autowired
	private WireMockProperties wireMockProperties;

	@Test
	public void contextLoads() throws Exception {
		assertThat(this.service.go()).isEqualTo("Hello World");
	}

	@Test
	public void portsAreNotFixed() {
		boolean httpPortDynamic = this.wireMockProperties.getServer().isPortDynamic();
		boolean httpsPortDynamic = this.wireMockProperties.getServer().isHttpsPortDynamic();
		assertThat(!httpPortDynamic || !httpsPortDynamic).isFalse();
	}

}
