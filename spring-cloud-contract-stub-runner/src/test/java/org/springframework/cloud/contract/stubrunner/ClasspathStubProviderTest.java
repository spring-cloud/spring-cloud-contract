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

package org.springframework.cloud.contract.stubrunner;

import org.junit.Test;

import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;

import static org.assertj.core.api.BDDAssertions.then;

/**
 * @author Marcin Grzejszczak
 */
public class ClasspathStubProviderTest {

	@Test
	public void should_return_null_if_stub_mode_is_not_classpath() {
		StubDownloader stubDownloader = new ClasspathStubProvider()
				.build(new StubRunnerOptionsBuilder().withStubsMode(StubRunnerProperties.StubsMode.REMOTE).build());

		then(stubDownloader).isNull();
	}

}
