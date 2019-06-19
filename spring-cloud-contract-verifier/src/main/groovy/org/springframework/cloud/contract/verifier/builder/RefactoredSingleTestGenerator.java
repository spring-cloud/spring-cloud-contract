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

package org.springframework.cloud.contract.verifier.builder;

import java.util.Collection;

import org.springframework.cloud.contract.verifier.config.ContractVerifierConfigProperties;
import org.springframework.cloud.contract.verifier.file.ContractMetadata;

/**
 * TODO
 *
 * @author Olga Maciaszek-Sharma
 * @author Marcin Grzejszczak
 * @since 2.2.0
 */
public class RefactoredSingleTestGenerator implements SingleTestGenerator {

	@Override
	public String buildClass(ContractVerifierConfigProperties properties,
			Collection<ContractMetadata> listOfFiles, String className,
			String classPackage, String includedDirectoryRelativePath) {
		throw new UnsupportedOperationException("Deprecated method");
	}

	@Override
	public String buildClass(ContractVerifierConfigProperties properties,
			Collection<ContractMetadata> listOfFiles,
			String includedDirectoryRelativePath, GeneratedClassData generatedClassData) {
		BlockBuilder builder = new BlockBuilder("\t");
		GeneratedClassMetaData metaData = new GeneratedClassMetaData(properties,
				listOfFiles, includedDirectoryRelativePath, generatedClassData);
		return classAsString(builder, metaData);
	}

	private String classAsString(BlockBuilder builder, GeneratedClassMetaData metaData) {
		SingleMethodBuilder methodBuilder = singleMethodBuilder(builder, metaData);
		ClassBodyBuilder bodyBuilder = classBodyBuilder(builder, metaData, methodBuilder);
		GeneratedTestClass generatedTestClass = generatedTestClass(builder, metaData,
				bodyBuilder);
		return generatedTestClass.asClassString();
	}

	GeneratedTestClass generatedTestClass(BlockBuilder builder,
			GeneratedClassMetaData metaData, ClassBodyBuilder bodyBuilder) {
		return GeneratedTestClassBuilder.builder(builder, metaData)
				.classBodyBuilder(bodyBuilder).metaData().java().groovy().build()
				.imports().defaultImports().custom().json().jUnit4().jUnit5().spock()
				.xml().messaging().restAssured().jaxRs().build().classAnnotations()
				.jUnit4().jUnit5().spock().build().build();
	}

	ClassBodyBuilder classBodyBuilder(BlockBuilder builder,
			GeneratedClassMetaData metaData, SingleMethodBuilder methodBuilder) {
		return ClassBodyBuilder.builder(builder, metaData).field().messaging().build()
				.methodBuilder(methodBuilder);
	}

	SingleMethodBuilder singleMethodBuilder(BlockBuilder builder,
			GeneratedClassMetaData metaData) {
		return SingleMethodBuilder.builder(builder, metaData).methodAnnotation().jUnit4()
				.jUnit5().build().methodMetadata().jUnit().spock().build().restAssured()
				.jaxRs().messaging();
	}

	@Override
	public String fileExtension(ContractVerifierConfigProperties properties) {
		return properties.getTestFramework().getClassExtension();
	}

}