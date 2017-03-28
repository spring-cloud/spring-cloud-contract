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

package org.springframework.cloud.contract.verifier

import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import groovy.util.logging.Slf4j
import org.springframework.cloud.contract.verifier.builder.SingleTestGenerator
import org.springframework.cloud.contract.verifier.config.ContractVerifierConfigProperties

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption

import static org.springframework.cloud.contract.verifier.util.NamesUtil.beforeLast
import static org.springframework.cloud.contract.verifier.util.NamesUtil.capitalize
import static org.springframework.cloud.contract.verifier.util.NamesUtil.packageToDirectory

@CompileStatic
@PackageScope
@Slf4j
class FileSaver {

	private final File targetDirectory
	private final SingleTestGenerator generator
	private final ContractVerifierConfigProperties properties

	FileSaver(File targetDirectory, SingleTestGenerator generator, ContractVerifierConfigProperties properties) {
		this.targetDirectory = targetDirectory
		this.generator = generator
		this.properties = properties
	}

	void saveClassFile(String fileName, String basePackageClass, String includedDirectoryRelativePath, byte[] classBytes) {
		Path testBaseDir = Paths.get(targetDirectory.absolutePath, packageToDirectory(basePackageClass),
				beforeLast(includedDirectoryRelativePath, File.separator))
		Files.createDirectories(testBaseDir)
		Path classPath = Paths.get(testBaseDir.toString(), capitalize(fileName) + generator.fileExtension(this.properties)).toAbsolutePath()
		log.info("Creating new class file [$classPath]")
		Files.write(classPath, classBytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)
	}

}
