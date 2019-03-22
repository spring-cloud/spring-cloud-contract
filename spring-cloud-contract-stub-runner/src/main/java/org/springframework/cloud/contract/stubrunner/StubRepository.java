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

package org.springframework.cloud.contract.stubrunner;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.cloud.contract.spec.Contract;
import org.springframework.cloud.contract.spec.ContractConverter;
import org.springframework.cloud.contract.stubrunner.provider.wiremock.WireMockHttpServerStub;
import org.springframework.cloud.contract.verifier.util.ContractScanner;
import org.springframework.core.io.support.SpringFactoriesLoader;

/**
 * Wraps the folder with stub mappings.
 */
class StubRepository {

	private static final Log log = LogFactory.getLog(StubRepository.class);

	final List<File> stubs;

	final Collection<Contract> contracts;

	private final File path;

	private final List<ContractConverter> contractConverters;

	private final List<HttpServerStub> httpServerStubs;

	private final StubRunnerOptions options;

	StubRepository(File repository, List<HttpServerStub> httpServerStubs,
			StubRunnerOptions options) {
		if (!repository.isDirectory()) {
			throw new IllegalArgumentException(
					"Missing descriptor repository under path [" + repository + "]");
		}
		this.contractConverters = SpringFactoriesLoader
				.loadFactories(ContractConverter.class, null);
		if (log.isTraceEnabled()) {
			log.trace(
					"Found the following contract converters " + this.contractConverters);
		}
		this.httpServerStubs = httpServerStubs;
		this.path = repository;
		this.options = options;
		this.stubs = stubs();
		this.contracts = contracts();
		if (log.isTraceEnabled()) {
			log.trace("Found the following contracts " + this.contracts);
		}
	}

	StubRepository(File repository) {
		this(repository, new ArrayList<>(), new StubRunnerOptionsBuilder().build());
	}

	public File getPath() {
		return this.path;
	}

	public List<File> getStubs() {
		return this.stubs;
	}

	public Collection<Contract> getContracts() {
		return this.contracts;
	}

	/**
	 * @return a list of contracts
	 */
	private Collection<Contract> contracts() {
		return new ArrayList<>(contractDescriptors());
	}

	/**
	 * @return the list of stubs
	 */
	private List<File> stubs() {
		return new ArrayList<>(collectedStubs());
	}

	private List<File> collectedStubs() {
		return this.path.exists() ? collectMappings(this.path)
				: Collections.<File>emptyList();
	}

	private List<File> collectMappings(File descriptorsDirectory) {
		final List<File> mappingDescriptors = new ArrayList<>();
		try {
			Files.walkFileTree(Paths.get(descriptorsDirectory.toURI()),
					new SimpleFileVisitor<Path>() {
						@Override
						public FileVisitResult visitFile(Path path,
								BasicFileAttributes attrs) throws IOException {
							File file = path.toFile();
							if (httpServerStubAccepts(file)
									&& isStubPerConsumerPathMatching(file)) {
								mappingDescriptors.add(file);
							}
							return super.visitFile(path, attrs);
						}
					});
		}
		catch (IOException e) {
			log.warn("Exception occurred while trying to parse file", e);
		}
		return mappingDescriptors;
	}

	private ContractConverter contractConverter(File file) {
		for (ContractConverter converter : this.contractConverters) {
			if (converter.isAccepted(file)) {
				return converter;
			}
		}
		return null;
	}

	private boolean httpServerStubAccepts(File file) {
		for (HttpServerStub httpServerStub : this.httpServerStubs) {
			if (httpServerStub.isAccepted(file)) {
				return true;
			}
		}
		// the default implementation
		return new WireMockHttpServerStub().isAccepted(file);
	}

	private Collection<Contract> contractDescriptors() {
		return (this.path.exists()
				? ContractScanner.collectContractDescriptors(this.path,
						this::isStubPerConsumerPathMatching)
				: Collections.<Contract>emptySet());
	}

	private boolean isStubPerConsumerPathMatching(File file) {
		if (!this.options.isStubsPerConsumer()) {
			return true;
		}
		String consumerName = this.options.getConsumerName();
		String searchedConsumerName = File.separator + consumerName + File.separator;
		String absolutePath = file.getAbsolutePath();
		boolean stubPerConsumerMatching = absolutePath.contains(searchedConsumerName);
		if (log.isDebugEnabled()) {
			log.debug("Absolute path [" + absolutePath + "] contains ["
					+ searchedConsumerName + "] in its path [" + stubPerConsumerMatching
					+ "]");
		}
		return stubPerConsumerMatching;
	}

}
