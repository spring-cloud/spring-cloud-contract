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

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import org.assertj.core.api.BDDAssertions;
import org.junit.Test;

import org.springframework.cloud.contract.spec.Contract;

/**
 * @author Sven Bayer
 */
public class StubRepositoryTest {

	private static final File YAML_REPOSITORY_LOCATION = new File(
			"src/test/resources/customYamlRepository");

	@Test
	public void should_prefer_custom_yaml_converter_over_standard() {
		// given:
		StubRepository repository = new StubRepository(YAML_REPOSITORY_LOCATION,
				new ArrayList<>(), new StubRunnerOptionsBuilder().build());
		int expectedDescriptorsSize = 1;

		// when:
		Collection<Contract> descriptors = repository.getContracts();

		// then:
		BDDAssertions.then(descriptors).hasSize(expectedDescriptorsSize);
	}

}
