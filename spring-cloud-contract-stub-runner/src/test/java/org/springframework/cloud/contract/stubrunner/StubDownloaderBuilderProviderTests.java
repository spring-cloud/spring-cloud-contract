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

import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author Marcin Grzejszczak
 */
@RunWith(MockitoJUnitRunner.class)
public class StubDownloaderBuilderProviderTests {

	@Mock
	StubDownloaderBuilder one;

	@Mock
	StubDownloaderBuilder two;

	@Mock
	StubDownloaderBuilder three;

	@Test
	public void should_get_providers_from_factories_default_and_additional_ones() {
		StubDownloaderBuilderProvider provider = new StubDownloaderBuilderProvider(
				Collections.singletonList(this.one)) {
			@Override
			List<StubDownloaderBuilder> defaultStubDownloaderBuilders() {
				return Collections.singletonList(StubDownloaderBuilderProviderTests.this.two);
			}
		};
		StubRunnerOptions options = new StubRunnerOptionsBuilder().withFailOnNoStubs(false).build();

		provider.get(options, this.three).downloadAndUnpackStubJar(new StubConfiguration("a:b:c"));

		BDDMockito.then(this.one).should().build(options);
		BDDMockito.then(this.two).should().build(options);
		BDDMockito.then(this.three).should().build(options);
	}

}
