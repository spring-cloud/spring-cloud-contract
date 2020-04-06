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

import org.springframework.cloud.contract.spec.internal.FromFileProperty;
import org.springframework.cloud.contract.verifier.file.SingleContractMetadata;
import org.springframework.util.Assert;

class BodyReader {

	private final GeneratedClassMetaData generatedClassMetaData;

	BodyReader(GeneratedClassMetaData generatedClassMetaData) {
		this.generatedClassMetaData = generatedClassMetaData;
	}

	String readBytesFromFileString(SingleContractMetadata metadata,
			FromFileProperty property, CommunicationType side) {
		String fileName = byteBodyToAFileForTestMethod(metadata, property, side);
		return "fileToBytes(this, \"" + fileName + "\")";
	}

	String readStringFromFileString(SingleContractMetadata metadata,
			FromFileProperty property, CommunicationType side) {
		if (!Charset.defaultCharset().toString().equals(property.getCharset())) {
			return "new String(" + readBytesFromFileString(metadata, property, side)
					+ ", \"" + property.getCharset() + "\")";
		}
		return "new String(" + readBytesFromFileString(metadata, property, side) + ")";
	}

	private String byteBodyToAFileForTestMethod(SingleContractMetadata metadata,
			FromFileProperty property, CommunicationType side) {
		GeneratedClassDataForMethod classDataForMethod = new GeneratedClassDataForMethod(
				this.generatedClassMetaData.generatedClassData, metadata.methodName());
		String newFileName = classDataForMethod.getMethodName() + "_"
				+ side.name().toLowerCase() + "_" + property.fileName();
		java.nio.file.Path parent = classDataForMethod.testClassPath().getParent();
		if (parent == null) {
			parent = classDataForMethod.testClassPath();
		}
		File newFile = new File(parent.toFile(), newFileName);
		// for IDE
		try {
			Files.write(newFile.toPath(), property.asBytes());
			// for plugin
			generatedTestResourcesFileBytes(property, newFile);
		}
		catch (IOException ex) {
			throw new IllegalStateException(ex);
		}
		return newFileName;
	}

	private void generatedTestResourcesFileBytes(FromFileProperty property, File newFile)
			throws IOException {
		Assert.notNull(
				this.generatedClassMetaData.configProperties.getGeneratedTestSourcesDir(),
				"No generated test sources directory set");
		Assert.notNull(
				this.generatedClassMetaData.configProperties
						.getGeneratedTestResourcesDir(),
				"No generated test resources directory set");
		Path path = this.generatedClassMetaData.configProperties
				.getGeneratedTestSourcesDir().toPath();
		Path relativePath = path.relativize(newFile.toPath());
		File newFileInGeneratedTestSources = new File(
				this.generatedClassMetaData.configProperties
						.getGeneratedTestResourcesDir(),
				relativePath.toString());
		newFileInGeneratedTestSources.getParentFile().mkdirs();
		Files.write(newFileInGeneratedTestSources.toPath(), property.asBytes());
	}

}
