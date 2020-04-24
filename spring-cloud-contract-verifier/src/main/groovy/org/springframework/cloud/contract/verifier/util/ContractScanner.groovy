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

package org.springframework.cloud.contract.verifier.util

import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import java.util.function.Predicate

import groovy.transform.CompileStatic
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import org.springframework.cloud.contract.spec.Contract
import org.springframework.cloud.contract.spec.ContractConverter
import org.springframework.cloud.contract.verifier.converter.YamlContractConverter
import org.springframework.core.io.support.SpringFactoriesLoader

/**
 * Scans through the given directory and converts all files for
 * contract definitions.
 *
 * @author Marcin Grzejszczak
 * @since 2.1.0
 */
@CompileStatic
final class ContractScanner {

	private static final Log log = LogFactory.getLog(ContractScanner.class)

	/**
	 * Traverses through the directories, applies converters
	 * to files that match them and converts the files to {@link Contract}.
	 * No additional file filtering takes place.
	 *
	 * @param rootDirectory - directory to traverse through
	 * @return collection of converted contracts
	 */
	@SuppressWarnings("unchecked")
	static Collection<Contract> collectContractDescriptors(
			final File rootDirectory) {
		return collectContractDescriptors(rootDirectory, { true })
	}

	/**
	 * Traverses through the directories, applies converters
	 * to files that match them and converts the files to {@link Contract}.
	 * Filters out files not matching a predicate.
	 *
	 * @param rootDirectory - directory to traverse through
	 * @param predicate - test applied against a file
	 * @return collection of converted contracts
	 */
	@SuppressWarnings("unchecked")
	static Collection<Contract> collectContractDescriptors(
			final File rootDirectory, Predicate<File> predicate) {
		final List<Contract> contractDescriptors = new ArrayList<>()
		try {
			Files.walkFileTree(Paths.get(rootDirectory.toURI()),
					new SimpleFileVisitor<Path>() {
						@Override
						FileVisitResult visitFile(Path path,
								BasicFileAttributes attrs) throws IOException {
							File file = path.toFile()
							ContractConverter converter = contractConverter(file)
							if (predicate.test(file)) {
								if (isContractDescriptor(file)) {
									contractDescriptors
											.addAll(ContractVerifierDslConverter
											.convertAsCollection(
											file.getParentFile(), file).findAll { it })
								}
								else if (converter != null
										&& converter.isAccepted(file)) {
									contractDescriptors
											.addAll(converter.convertFrom(file))
								}
								else if (YamlContractConverter.INSTANCE
															  .isAccepted(file)) {
									contractDescriptors
											.addAll(YamlContractConverter.INSTANCE
																		 .convertFrom(file))
								}
							}
							return super.visitFile(path, attrs)
						}
					})
		}
		catch (IOException e) {
			log.warn("Exception occurred while trying to parse file", e)
		}
		return contractDescriptors
	}

	private static ContractConverter contractConverter(File file) {
		for (ContractConverter converter : SpringFactoriesLoader
				.loadFactories(ContractConverter.class, null)) {
			if (converter.isAccepted(file)) {
				return converter
			}
		}
		return null
	}

	private static boolean isContractDescriptor(File file) {
		return ContractVerifierDslConverter.INSTANCE.isAccepted(file)
	}
}
