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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.cloud.contract.spec.Contract;
import org.springframework.cloud.contract.verifier.converter.StubGenerator;
import org.springframework.cloud.contract.verifier.converter.StubGeneratorProvider;
import org.springframework.cloud.contract.verifier.file.ContractMetadata;
import org.springframework.util.StringUtils;

final class MappingGenerator {

	private static final Log log = LogFactory.getLog(MappingGenerator.class);

	private MappingGenerator() {
		throw new IllegalStateException("Can't instantiate utility class");
	}

	static Collection<Path> toMappings(File contractFile, Collection<Contract> contracts,
			File mappingsFolder) {
		StubGeneratorProvider provider = new StubGeneratorProvider();
		Collection<StubGenerator> stubGenerators = provider
				.converterForName(contractFile.getName());
		if (log.isDebugEnabled()) {
			log.debug("Found following matching stub generators " + stubGenerators);
		}
		Collection<Path> mappings = new LinkedList<>();
		for (StubGenerator stubGenerator : stubGenerators) {
			Map<Contract, String> map = stubGenerator.convertContents(
					contractFile.getName(), new ContractMetadata(contractFile.toPath(),
							false, contracts.size(), null, contracts));
			for (Map.Entry<Contract, String> entry : map.entrySet()) {
				String value = entry.getValue();
				File mapping = new File(mappingsFolder,
						StringUtils.stripFilenameExtension(contractFile.getName()) + "_"
								+ Math.abs(entry.getKey().hashCode())
								+ stubGenerator.fileExtension());
				mappings.add(storeFile(mapping.toPath(), value.getBytes()));
			}
		}
		return mappings;
	}

	private static Path storeFile(Path path, byte[] contents) {
		try {
			Path storedPath = Files.write(path, contents);
			if (log.isDebugEnabled()) {
				log.debug("Stored file [" + path.toString() + "]");
			}
			return storedPath;
		}
		catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

}
