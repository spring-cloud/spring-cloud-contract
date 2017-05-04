/*
 *  Copyright 2013-2017 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.contract.spec.Contract;
import org.springframework.cloud.contract.spec.ContractConverter;
import org.springframework.cloud.contract.stubrunner.provider.wiremock.WireMockHttpServerStub;
import org.springframework.cloud.contract.verifier.util.ContractVerifierDslConverter;
import org.springframework.core.io.support.SpringFactoriesLoader;

/**
 * Wraps the folder with stub mappings.
 */
class StubRepository {

	private static final Logger log = LoggerFactory.getLogger(StubRepository.class);

	private final File path;
	final List<File> stubs;
	final Collection<Contract> contracts;
	private final List<ContractConverter> contractConverters;
	private final List<HttpServerStub> httpServerStubs;
	private final StubRunnerOptions options;

	StubRepository(File repository, List<HttpServerStub> httpServerStubs,
			StubRunnerOptions options) {
		if (!repository.isDirectory()) {
			throw new IllegalArgumentException(
					"Missing descriptor repository under path [" + repository + "]");
		}
		this.contractConverters = SpringFactoriesLoader.loadFactories(ContractConverter.class, null);
		if (log.isDebugEnabled()) {
			log.debug("Found the following contract converters " + this.contractConverters);
		}
		this.httpServerStubs = httpServerStubs;
		this.path = repository;
		this.stubs = stubs();
		this.options = options;
		this.contracts = contracts();
	}

	StubRepository(File repository) {
		this(repository, new ArrayList<HttpServerStub>(), new StubRunnerOptionsBuilder().build());
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
	 * Returns a list of contracts
	 */
	private Collection<Contract> contracts() {
		List<Contract> contracts = new ArrayList<>();
		contracts.addAll(contractDescriptors());
		return contracts;
	}

	/**
	 * Returns the list of stubs
	 */
	private List<File> stubs() {
		List<File> stubs = new ArrayList<>();
		stubs.addAll(collectedStubs());
		return stubs;
	}

	private List<File> collectedStubs() {
		return this.path.exists() ? collectMappings(this.path)
				: Collections.<File>emptyList();
	}

	private List<File> collectMappings(
			File descriptorsDirectory) {
		final List<File> mappingDescriptors = new ArrayList<>();
		try {
			Files.walkFileTree(Paths.get(descriptorsDirectory.toURI()),
					new SimpleFileVisitor<Path>() {
						@Override
						public FileVisitResult visitFile(Path path,
								BasicFileAttributes attrs) throws IOException {
							File file = path.toFile();
							if (httpServerStubAccepts(file) && isStubPerConsumerPathMatching(file)) {
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
		return (this.path.exists() ? collectContractDescriptors(this.path)
				: Collections.<Contract>emptySet());
	}

	@SuppressWarnings("unchecked")
	private Collection<Contract> collectContractDescriptors(File descriptorsDirectory) {
		final List<Contract> contractDescriptors = new ArrayList<>();
		try {
			Files.walkFileTree(Paths.get(descriptorsDirectory.toURI()),
					new SimpleFileVisitor<Path>() {
						@Override
						public FileVisitResult visitFile(Path path,
								BasicFileAttributes attrs) throws IOException {
							File file = path.toFile();
							ContractConverter converter = contractConverter(file);
							if (isContractDescriptor(file) && isStubPerConsumerPathMatching(file)) {
								contractDescriptors
								.addAll(ContractVerifierDslConverter.convertAsCollection(file));
							} else if (converter != null && isStubPerConsumerPathMatching(file)) {
								contractDescriptors.addAll(converter.convertFrom(file));
							}
							return super.visitFile(path, attrs);
						}
					});
		}
		catch (IOException e) {
			log.warn("Exception occurred while trying to parse file", e);
		}
		return contractDescriptors;
	}

	private boolean isStubPerConsumerPathMatching(File file) {
		if (!this.options.isStubsPerConsumer()) {
			return true;
		}
		String consumerName = this.options.getConsumerName();
		return file.getAbsolutePath().contains(File.separator + consumerName + File.separator);
	}

	private static boolean isContractDescriptor(File file) {
		// TODO: Consider script injections implications...
		return file.isFile() && file.getName().endsWith(".groovy");
	}

}
