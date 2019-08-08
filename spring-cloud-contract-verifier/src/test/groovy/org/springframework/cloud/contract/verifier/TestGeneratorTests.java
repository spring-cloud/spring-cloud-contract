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

package org.springframework.cloud.contract.verifier;

import java.io.File;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.assertj.core.api.BDDAssertions;
import org.junit.Test;
import org.mockito.BDDMockito;
import wiremock.com.google.common.collect.ArrayListMultimap;
import wiremock.com.google.common.collect.ListMultimap;

import org.springframework.cloud.contract.spec.Contract;
import org.springframework.cloud.contract.verifier.builder.SingleTestGenerator;
import org.springframework.cloud.contract.verifier.config.ContractVerifierConfigProperties;
import org.springframework.cloud.contract.verifier.file.ContractFileScanner;
import org.springframework.cloud.contract.verifier.file.ContractMetadata;

public class TestGeneratorTests {

	@Test
	public void should_throw_exception_when_in_progress_contracts_found() {
		// given:
		ContractVerifierConfigProperties properties = new ContractVerifierConfigProperties();
		properties.setFailOnInProgress(true);
		properties.setContractsDslDir(new File("."));
		SingleTestGenerator singleTestGenerator = BDDMockito
				.mock(SingleTestGenerator.class);
		FileSaver fileSaver = BDDMockito.mock(FileSaver.class);
		// and:
		ArrayListMultimap<Path, ContractMetadata> multimap = ArrayListMultimap.create();
		Path path = new File(".").toPath();
		multimap.put(path,
				new ContractMetadata(path, false, 0, null, Contract.make(it -> {
					it.inProgress();
					it.request(r -> {
						r.method(r.GET());
						r.url("/foo");
					});
					it.response(r -> {
						r.status(r.OK());
					});
				})));
		ContractFileScanner scanner = new ContractFileScanner(null, null, null) {
			@Override
			public ListMultimap<Path, ContractMetadata> findContracts() {
				return multimap;
			}
		};
		// and:
		TestGenerator testGenerator = new TestGenerator(properties, singleTestGenerator,
				fileSaver, scanner);

		// then:
		BDDAssertions.thenThrownBy(() -> {
			// when:
			testGenerator.generateTestClasses("com.example");
		}).isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("In progress contracts found in");
	}

	@Test
	public void should_not_throw_exception_when_in_progress_contracts_found_but_the_fail_on_in_progress_switch_is_off() {
		// given:
		ContractVerifierConfigProperties properties = new ContractVerifierConfigProperties();
		properties.setFailOnInProgress(false);
		properties.setContractsDslDir(new File("."));
		SingleTestGenerator singleTestGenerator = BDDMockito
				.mock(SingleTestGenerator.class);
		FileSaver fileSaver = BDDMockito.mock(FileSaver.class);
		// and:
		ArrayListMultimap<Path, ContractMetadata> multimap = ArrayListMultimap.create();
		Path path = new File(".").toPath();
		multimap.put(path,
				new ContractMetadata(path, false, 0, null, Contract.make(it -> {
					it.inProgress();
					it.request(r -> {
						r.method(r.GET());
						r.url("/foo");
					});
					it.response(r -> {
						r.status(r.OK());
					});
				})));
		ContractFileScanner scanner = new ContractFileScanner(null, null, null) {
			@Override
			public ListMultimap<Path, ContractMetadata> findContracts() {
				return multimap;
			}
		};
		// and:
		TestGenerator testGenerator = new TestGenerator(properties, singleTestGenerator,
				fileSaver, scanner) {
			@Override
			Set<Map.Entry<Path, Collection<ContractMetadata>>> processAllNotInProgress(
					ListMultimap<Path, ContractMetadata> contracts,
					String basePackageName) {
				return null;
			}
		};

		// when:
		testGenerator.generateTestClasses("com.example");

		// then: noExceptionThrown()
	}

}
