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

package org.springframework.cloud.contract.stubrunner

import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import groovy.util.logging.Slf4j
import org.springframework.cloud.contract.spec.Contract
import org.springframework.cloud.contract.verifier.util.ContractVerifierDslConverter

/**
 * Wraps the folder with WireMock mappings.
 */
@CompileStatic
@PackageScope
@Slf4j
class StubRepository {

	private final File path
	final List<WiremockMappingDescriptor> projectDescriptors
	final Collection<Contract> contracts

	StubRepository(File repository) {
		if (!repository.isDirectory()) {
			throw new FileNotFoundException("Missing descriptor repository under path [$path]")
		}
		this.path = repository
		this.projectDescriptors = projectDescriptors()
		this.contracts = contracts()
	}

	/**
	 * Returns a list of {@link Contract}
	 */
	private Collection<Contract> contracts() {
		List<Contract> contracts = []
		contracts.addAll(contractDescriptors())
		return contracts
	}

	/**
	 * Returns the list of WireMock JSON files wrapped in {@link WiremockMappingDescriptor}
	 */
	private List<WiremockMappingDescriptor> projectDescriptors() {
		List<WiremockMappingDescriptor> mappingDescriptors = []
		mappingDescriptors.addAll(contextDescriptors())
		return mappingDescriptors
	}

	private List<WiremockMappingDescriptor> contextDescriptors() {
		return path.exists() ? collectMappingDescriptors(path) : []
	}

	private List<WiremockMappingDescriptor> collectMappingDescriptors(File descriptorsDirectory) {
		List<WiremockMappingDescriptor> mappingDescriptors = []
		descriptorsDirectory.eachFileRecurse { File file ->
			if (isMappingDescriptor(file)) {
				mappingDescriptors << new WiremockMappingDescriptor(file)
			}
		}
		return mappingDescriptors
	}

	private Collection<Contract> contractDescriptors() {
		return path.exists() ? collectContractDescriptors(path) : []
	}

	private Collection<Contract> collectContractDescriptors(File descriptorsDirectory) {
		List<Contract> mappingDescriptors = []
		descriptorsDirectory.eachFileRecurse { File file ->
			if (isContractDescriptor(file)) {
				try {
					mappingDescriptors << ContractVerifierDslConverter.convert(file)
				} catch (Exception e) {
					log.warn("Exception occurred while trying to parse file [$file]", e)
				}
			}
		}
		return mappingDescriptors
	}

	private static boolean isMappingDescriptor(File file) {
		return file.isFile() && file.name.endsWith('.json')
	}

	private static boolean isContractDescriptor(File file) {
		//TODO: Consider script injections implications...
		return file.isFile() && file.name.endsWith('.groovy')
	}

}
