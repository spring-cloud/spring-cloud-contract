/*
 *  Copyright 2013-2016 the original author or authors.
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
import org.springframework.cloud.contract.verifier.util.ContractVerifierDslConverter;

/**
 * Wraps the folder with WireMock mappings.
 */
class StubRepository {

	private static final Logger log = LoggerFactory.getLogger(StubRepository.class);

	private final File path;
	final List<WiremockMappingDescriptor> projectDescriptors;
	final Collection<Contract> contracts;

	public StubRepository(File repository) {
		if (!repository.isDirectory()) {
			throw new IllegalArgumentException(
					"Missing descriptor repository under path [" + repository + "]");
		}
		this.path = repository;
		this.projectDescriptors = projectDescriptors();
		this.contracts = contracts();
	}

	public File getPath() {
		return this.path;
	}

	public List<WiremockMappingDescriptor> getProjectDescriptors() {
		return this.projectDescriptors;
	}

	public Collection<Contract> getContracts() {
		return this.contracts;
	}

	/**
	 * Returns a list of {@link Contract}
	 */
	private Collection<Contract> contracts() {
		List<Contract> contracts = new ArrayList<>();
		contracts.addAll(contractDescriptors());
		return contracts;
	}

	/**
	 * Returns the list of WireMock JSON files wrapped in
	 * {@link WiremockMappingDescriptor}
	 */
	private List<WiremockMappingDescriptor> projectDescriptors() {
		List<WiremockMappingDescriptor> mappingDescriptors = new ArrayList<>();
		mappingDescriptors.addAll(contextDescriptors());
		return mappingDescriptors;
	}

	private List<WiremockMappingDescriptor> contextDescriptors() {
		return this.path.exists() ? collectMappingDescriptors(this.path)
				: Collections.<WiremockMappingDescriptor>emptyList();
	}

	private List<WiremockMappingDescriptor> collectMappingDescriptors(
			File descriptorsDirectory) {
		final List<WiremockMappingDescriptor> mappingDescriptors = new ArrayList<>();
		try {
			Files.walkFileTree(Paths.get(descriptorsDirectory.toURI()),
					new SimpleFileVisitor<Path>() {
						@Override
						public FileVisitResult visitFile(Path path,
								BasicFileAttributes attrs) throws IOException {
							File file = path.toFile();
							if (isMappingDescriptor(file)) {
								mappingDescriptors
										.add(new WiremockMappingDescriptor(file));
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

	private Collection<Contract> contractDescriptors() {
		return (this.path.exists() ? collectContractDescriptors(this.path)
				: Collections.<Contract>emptySet());
	}

	private Collection<Contract> collectContractDescriptors(File descriptorsDirectory) {
		final List<Contract> mappingDescriptors = new ArrayList<>();
		try {
			Files.walkFileTree(Paths.get(descriptorsDirectory.toURI()),
					new SimpleFileVisitor<Path>() {
						@Override
						public FileVisitResult visitFile(Path path,
								BasicFileAttributes attrs) throws IOException {
							File file = path.toFile();
							if (isContractDescriptor(file)) {
								mappingDescriptors
								.addAll(ContractVerifierDslConverter.convertAsCollection(file));
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

	private static boolean isMappingDescriptor(File file) {
		return file.isFile() && file.getName().endsWith(".json");
	}

	private static boolean isContractDescriptor(File file) {
		// TODO: Consider script injections implications...
		return file.isFile() && file.getName().endsWith(".groovy");
	}

}
