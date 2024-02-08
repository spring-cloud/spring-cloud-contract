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

package org.springframework.cloud.contract.verifier.converter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.cloud.contract.spec.Contract;
import org.springframework.cloud.contract.spec.ContractConverter;
import org.springframework.cloud.contract.verifier.util.ContractVerifierDslConverter;
import org.springframework.core.io.support.SpringFactoriesLoader;

/**
 * Converts contracts to YAML for the given folder.
 *
 * @author Marcin Grzejszczak
 * @since 2.1.0
 */
public final class ToYamlConverter {

	private static final Logger log = LoggerFactory.getLogger(ToYamlConverter.class);

	private ToYamlConverter() {
		throw new IllegalStateException("Can't instantiate a utility class");
	}

	private static final YamlContractConverter yamlContractConverter = new YamlContractConverter();

	private static final List<ContractConverter> CONTRACT_CONVERTERS = converters();

	protected static void doReplaceContractWithYaml(ContractConverter converter, File file) {
		if (log.isDebugEnabled()) {
			log.debug("Will replace contract [{}] with a YAML version", file.getName());
		}
		// base dir: target/copied_contracts/contracts/
		// target/copied_contracts/contracts/foo/baz/bar.groovy
		Collection<Contract> collection = converter.convertFrom(file);
		if (log.isDebugEnabled()) {
			log.debug("Converted file [{}] to collection of [{}] contracts", file, collection.size());
		}
		List<YamlContract> yamls = yamlContractConverter.convertTo(collection);
		if (log.isDebugEnabled()) {
			log.debug("Converted collection of [{}] contracts to [{}] YAML contracts", collection.size(), yamls.size());
		}
		// rm target/copied_contracts/contracts/foo/baz/bar.groovy
		file.delete();
		// [contracts/foo/baz/bar.groovy] -> [contracts/foo/baz/bar.yml]
		Map<String, byte[]> stored = yamlContractConverter.store(yamls);
		if (log.isDebugEnabled()) {
			log.debug("Dumped YAMLs to following file names {}", stored.keySet());
		}
		stored.forEach((key, value) -> {
			File ymlContractVersion = new File(file.getParentFile(), key);
			// store the YMLs instead of groovy files
			try {
				Files.write(ymlContractVersion.toPath(), value);
			}
			catch (IOException e) {
				throw new RuntimeException(e);
			}
			if (log.isDebugEnabled()) {
				log.debug("Written file [{}] with YAML contract definition", ymlContractVersion);
			}
		});
	}

	/**
	 * If a contract ends with e.g. [.groovy] we will move it to the [original] folder,
	 * convert the [.groovy] version to [.yml] and store it instead of the Groovy file.
	 * From that we will continue processing as if from the very beginning there was only
	 * a [.yml] file
	 * @param baseDir base directory for contracts
	 */
	public static void replaceContractWithYaml(File baseDir) {
		try {
			Files.walk(baseDir.toPath()).map(Path::toFile)
					.forEach(file -> CONTRACT_CONVERTERS.stream().filter(converter -> converter.isAccepted(file))
							.findFirst().ifPresent(converter -> doReplaceContractWithYaml(converter, file)));
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static List<ContractConverter> converters() {
		List<ContractConverter> converters = SpringFactoriesLoader.loadFactories(ContractConverter.class, null);
		converters.add(YamlContractConverter.INSTANCE);
		converters.add(ContractVerifierDslConverter.INSTANCE);
		return converters;
	}

}
