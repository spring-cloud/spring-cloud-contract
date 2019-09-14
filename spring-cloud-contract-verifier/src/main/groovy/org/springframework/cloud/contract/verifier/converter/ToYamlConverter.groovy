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

package org.springframework.cloud.contract.verifier.converter

import java.nio.file.Files

import groovy.transform.CompileStatic
import groovy.util.logging.Commons

import org.springframework.cloud.contract.spec.Contract
import org.springframework.cloud.contract.spec.ContractConverter
import org.springframework.cloud.contract.verifier.util.ContractVerifierDslConverter
import org.springframework.core.io.support.SpringFactoriesLoader

/**
 * Converts contracts to YAML for the given folder
 *
 * @author Marcin Grzejszczak
 * @since 2.1.0
 */
@CompileStatic
@Commons
class ToYamlConverter {

	private ToYamlConverter() {
		throw new IllegalStateException("Can't instantiate a utility class")
	}

	private static YamlContractConverter yamlContractConverter = new YamlContractConverter()

	private static final List<ContractConverter> CONTRACT_CONVERTERS = converters()

	protected static void doReplaceContractWithYaml(ContractConverter converter, File file) {
		// base dir: target/copied_contracts/contracts/
		// target/copied_contracts/contracts/foo/baz/bar.groovy
		Collection<Contract> collection = converter.convertFrom(file)
		if (log.isDebugEnabled()) {
			log.debug("Converted file [" + file + "] to collection of ["
					+ collection.
					size()
					+ "] contracts")
		}
		List<YamlContract> yamls = yamlContractConverter.convertTo(collection)
		if (log.isDebugEnabled()) {
			log.debug("Converted collection of ["
					+ collection.
					size()
					+ "] contracts to ["
					+ yamls.size()
					+ "] YAML contracts")
		}
		// rm target/copied_contracts/contracts/foo/baz/bar.groovy
		file.delete()
		// [contracts/foo/baz/bar.groovy] -> [contracts/foo/baz/bar.yml]
		Map<String, byte[]> stored = yamlContractConverter.store(yamls)
		if (log.isDebugEnabled()) {
			log.debug("Dumped YAMLs to following file names " + stored.keySet())
		}
		stored.entrySet().each {
			File ymlContractVersion = new File(file.parentFile, it.getKey())
			// store the YMLs instead of groovy files
			Files.write(ymlContractVersion.toPath(), it.getValue())
			if (log.isDebugEnabled()) {
				log.debug("Written file [" + ymlContractVersion + "] with YAML contract definition")
			}
		}
	}

	/**
	 * If a contract ends with e.g. [.groovy] we will move it to the [original]
	 * folder, convert the [.groovy] version to [.yml] and store it instead
	 * of the Groovy file. From that we will continue processing as if
	 * from the very beginning there was only a [.yml] file
	 *
	 * @param baseDir
	 */
	static void replaceContractWithYaml(File baseDir) {
		baseDir.eachFileRecurse { File file ->
			ContractConverter converter = CONTRACT_CONVERTERS.find {
				it.isAccepted(file)
			}
			if (converter) {
				if (log.isDebugEnabled()) {
					log.debug("Will replace contract [${file.name}] with a YAML version")
				}
				doReplaceContractWithYaml(converter, file)
			}
		}
	}

	private static List<ContractConverter> converters() {
		List<ContractConverter> converters =
				SpringFactoriesLoader.loadFactories(ContractConverter, null)
		converters.add(YamlContractConverter.INSTANCE)
		converters.add(ContractVerifierDslConverter.INSTANCE)
		return converters
	}
}
