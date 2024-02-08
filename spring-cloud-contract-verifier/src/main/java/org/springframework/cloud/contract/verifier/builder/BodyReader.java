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

package org.springframework.cloud.contract.verifier.builder;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.cloud.contract.spec.Contract;
import org.springframework.cloud.contract.spec.internal.FromFileProperty;
import org.springframework.cloud.contract.verifier.converter.YamlContract;
import org.springframework.cloud.contract.verifier.converter.YamlContractConverter;
import org.springframework.cloud.contract.verifier.file.SingleContractMetadata;
import org.springframework.util.Assert;

class BodyReader {

	private static final Log log = LogFactory.getLog(BodyReader.class);

	private final GeneratedClassMetaData generatedClassMetaData;

	private final YamlContractConverter converter = new YamlContractConverter();

	BodyReader(GeneratedClassMetaData generatedClassMetaData) {
		this.generatedClassMetaData = generatedClassMetaData;
	}

	String readBytesFromFileString(SingleContractMetadata metadata, FromFileProperty property, CommunicationType side) {
		String fileName = byteBodyToAFileForTestMethod(metadata, property, side);
		return "fileToBytes(this, \"" + fileName + "\")";
	}

	String readStringFromFileString(SingleContractMetadata metadata, FromFileProperty property,
			CommunicationType side) {
		if (!Charset.defaultCharset().toString().equals(property.getCharset())) {
			return "new String(" + readBytesFromFileString(metadata, property, side) + ", \"" + property.getCharset()
					+ "\")";
		}
		return "new String(" + readBytesFromFileString(metadata, property, side) + ")";
	}

	void storeContractAsYaml(SingleContractMetadata metadata) {
		Contract contract = metadata.getContract();
		List<YamlContract> contracts = this.converter.convertTo(Collections.singleton(contract));
		Map<String, byte[]> store = this.converter.store(contracts);
		store.forEach((name, bytes) -> writeFileForBothIdeAndBuildTool(metadata, bytes, name));
	}

	private String byteBodyToAFileForTestMethod(SingleContractMetadata metadata, FromFileProperty property,
			CommunicationType side) {
		GeneratedClassDataForMethod classDataForMethod = classDataForMethod(metadata);
		String newFileName = classDataForMethod.getMethodName() + "_" + side.name().toLowerCase() + "_"
				+ property.fileName();
		writeFileForBothIdeAndBuildTool(metadata, property.asBytes(), newFileName);
		return newFileName;
	}

	private GeneratedClassDataForMethod classDataForMethod(SingleContractMetadata metadata) {
		return new GeneratedClassDataForMethod(this.generatedClassMetaData.generatedClassData, metadata.methodName());
	}

	private void writeFileForBothIdeAndBuildTool(SingleContractMetadata metadata, byte[] bytes, String newFileName) {
		GeneratedClassDataForMethod classDataForMethod = classDataForMethod(metadata);
		java.nio.file.Path parent = classDataForMethod.testClassPath().getParent();
		if (parent == null) {
			parent = classDataForMethod.testClassPath();
		}
		File newFile = new File(parent.toFile(), newFileName);
		if (newFile.exists()) {
			return;
		}
		// for IDE
		try {
			Path path = newFile.toPath();
			if (log.isDebugEnabled()) {
				log.debug("Writing file for [" + path + "] for body reading in generated test (for IDE)");
			}
			Files.write(path, bytes);
			// for plugin
			generatedTestResourcesFileBytes(bytes, newFile);
		}
		catch (IOException ex) {
			throw new IllegalStateException(ex);
		}
	}

	private void generatedTestResourcesFileBytes(byte[] bytes, File newFile) throws IOException {
		Assert.notNull(this.generatedClassMetaData.configProperties.getGeneratedTestSourcesDir(),
				"No generated test sources directory set");
		Assert.notNull(this.generatedClassMetaData.configProperties.getGeneratedTestResourcesDir(),
				"No generated test resources directory set");
		Path path = this.generatedClassMetaData.configProperties.getGeneratedTestSourcesDir().toPath();
		Path relativePath = path.relativize(newFile.toPath());
		File newFileInGeneratedTestSources = new File(
				this.generatedClassMetaData.configProperties.getGeneratedTestResourcesDir(), relativePath.toString());
		newFileInGeneratedTestSources.getParentFile().mkdirs();
		Path generatedTestSourceFilePath = newFileInGeneratedTestSources.toPath();
		if (log.isDebugEnabled()) {
			log.debug("Writing file for [" + generatedTestSourceFilePath
					+ "] for body reading in generated test (Build tool)");
		}
		Files.write(generatedTestSourceFilePath, bytes);
	}

}
