/*
 * Copyright 2013-2019 the original author or authors.
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

package contracts;

import io.restassured.RestAssured;
import org.junit.Before;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.StringUtils;

/**
 * @author Marcin Grzejszczak
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = RestBase.Config.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
public abstract class RestBase {

	@Value("${APPLICATION_BASE_URL}")
	String url;

	@Value("${APPLICATION_USERNAME:}")
	String username;

	@Value("${APPLICATION_PASSWORD:}")
	String password;

	@Before
	public void setup() {
		RestAssured.baseURI = this.url;
		if (StringUtils.hasText(this.username)) {
			RestAssured.authentication = RestAssured.basic(this.username, this.password);
		}
	}

	@Configuration
	@EnableAutoConfiguration
	protected static class Config {

	}

}
