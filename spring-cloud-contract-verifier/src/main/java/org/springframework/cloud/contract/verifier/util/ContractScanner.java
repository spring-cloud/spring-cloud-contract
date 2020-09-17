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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.cloud.contract.spec.Contract;
import org.springframework.cloud.contract.spec.ContractConverter;
import org.springframework.cloud.contract.verifier.converter.YamlContractConverter;
import org.springframework.core.io.support.SpringFactoriesLoader;

/**
 * Scans through the given directory and converts all files for contract definitions.
 *
 * @author Marcin Grzejszczak
 * @author Anatolii Zhmaiev
 * @since 2.1.0
 */
public final class ContractScanner {

	private static final Log log = LogFactory.getLog(ContractScanner.class);

	private ContractScanner() {
		throw new IllegalStateException("Can't instantiate an utility class");
	}

	/**
	 * Traverses through the directories, applies converters to files that match them and
	 * converts the files to {@link Contract}. No additional file filtering takes place.
	 * @param rootDirectory - directory to traverse through
	 * @return collection of converted contracts
	 */
	public static Collection<Contract> collectContractDescriptors(File rootDirectory) {
		return collectContractDescriptors(rootDirectory, (file) -> true);
	}

	/**
	 * Traverses through the directories, applies converters to files that match them and
	 * converts the files to {@link Contract}. Filters out files not matching a predicate.
	 * @param rootDirectory - directory to traverse through
	 * @param predicate - test applied against a file
	 * @return collection of converted contracts
	 */
	public static Collection<Contract> collectContractDescriptors(File rootDirectory, Predicate<File> predicate) {
		try {
			return Files.walk(rootDirectory.toPath()).map(Path::toFile).filter(file -> !file.isDirectory())
					.filter(predicate).map(ContractScanner::doCollectContractDescriptors).flatMap(Collection::stream)
					.collect(Collectors.toList());
		}
		catch (IOException e) {
			log.warn("Exception occurred while trying to parse file", e);
			return Collections.emptyList();
		}
	}

	private static Collection<Contract> doCollectContractDescriptors(File file) {
		if (isContractDescriptor(file)) {
			return ContractVerifierDslConverter.convertAsCollection(file.getParentFile(), file);
		}
		ContractConverter<?> converter = contractConverter(file);
		if (converter != null && converter.isAccepted(file)) {
			return converter.convertFrom(file);
		}
		if (YamlContractConverter.INSTANCE.isAccepted(file)) {
			return YamlContractConverter.INSTANCE.convertFrom(file);
		}
		return Collections.emptyList();
	}

	private static ContractConverter<?> contractConverter(File file) {
		return SpringFactoriesLoader.loadFactories(ContractConverter.class, null).stream()
				.filter(converter -> converter.isAccepted(file)).findFirst().orElse(null);
	}

	private static boolean isContractDescriptor(File file) {
		return ContractVerifierDslConverter.INSTANCE.isAccepted(file);
	}

}
