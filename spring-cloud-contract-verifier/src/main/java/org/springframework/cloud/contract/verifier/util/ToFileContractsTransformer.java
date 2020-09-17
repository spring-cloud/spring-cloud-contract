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

package org.springframework.cloud.contract.verifier.util;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.cloud.contract.spec.Contract;
import org.springframework.cloud.contract.spec.ContractConverter;

import static org.springframework.cloud.contract.verifier.util.ContractScanner.collectContractDescriptors;

/**
 * Allows conversion of Contract files to files.
 *
 * WARNING: This class is incubating and experimental. It might change in the future.
 *
 * @author Marcin Grzejszczak
 * @since 2.1.0
 */
public final class ToFileContractsTransformer {

	private static final Logger log = LoggerFactory.getLogger(ToFileContractsTransformer.class);

	/**
	 * Dumps contracts as files for the given {@link ContractConverter}.
	 *
	 * - argument 1 : FQN - fully qualified name of the {@link ContractConverter}
	 * [REQUIRED] - argument 2 : path - path where the dumped files should be stored
	 * [OPTIONAL - defaults to target/converted-contracts] - argument 3 : path - path were
	 * the contracts should be searched for [OPTIONAL - defaults to
	 * src/test/resources/contracts]
	 */
	public static void main(String[] args) {
		if (args.length == 0) {
			throw new IllegalStateException(exceptionMessage());
		}
		log.warn("You're using an incubating feature. Note, that it can be changed / removed in the future");
		String fqn = args[0];
		String outputPath = args.length >= 2 ? args[1] : "target/converted-contracts";
		String path = args.length >= 3 ? args[2] : "src/test/resources/contracts";
		new ToFileContractsTransformer().storeContractsAsFiles(path, fqn, outputPath);
	}

	private static String exceptionMessage() {
		return "Please provide the FQN of the ContractConverter. E.g. [org.springframework.cloud.contract.verifier.converter.YamlContractConverter]";
	}

	/**
	 * @param path - path were the contracts should be searched for
	 * @param fqn - fully qualified name of the {@link ContractConverter}
	 * @param outputPath - path where the dumped files should be stored
	 * @return list of dumped files
	 */
	List<File> storeContractsAsFiles(String path, String fqn, String outputPath) {
		try {
			log.info("Input path [{}]", path);
			log.info("FQN of the converter [{}]", fqn);
			log.info("Output path [{}]", outputPath);
			Collection<Contract> contracts = collectContractDescriptors(new File(path));
			log.info("Found [{}] contract definition", contracts.size());
			Class<?> name = Class.forName(fqn);
			ContractConverter<Collection> contractConverter = (ContractConverter) name.newInstance();
			Collection converted = contractConverter.convertTo(contracts);
			log.info("Successfully converted contracts definitions");
			Map<String, byte[]> stored = contractConverter.store(converted);
			File outputFolder = new File(outputPath);
			outputFolder.mkdirs();
			int i = 1;
			Set<Map.Entry<String, byte[]>> entries = stored.entrySet();
			log.info("Will convert [{}] contracts", entries.size());
			List<File> files = new ArrayList<>();
			for (Map.Entry<String, byte[]> entry : entries) {
				File outputFile = new File(outputFolder, entry.getKey());
				Files.write(outputFile.toPath(), entry.getValue());
				log.info("[{}/{}] Successfully stored [{}]", i, entries.size(), outputFile.getName());
				files.add(outputFile);
			}
			return files;
		}
		catch (Exception ex) {
			throw new IllegalStateException(ex);
		}
	}

}
