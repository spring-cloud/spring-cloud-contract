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
 *
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

		SingleMethodBuilder methodBuilder = SingleMethodBuilder.builder(builder)
				.contractMetaData(metaData)
				// JUnitMethodAnnotation
				.methodAnnotation(new JUnit4MethodAnnotation(builder, metaData),
						new JUnit4IgnoreMethodAnnotation(builder, metaData))
				// JavaMethodMetadata
				// SpockMethodMetadata
				.methodMetadata(new JunitMethodMetadata(builder))
				.given(new RestAssuredGiven(builder, metaData))
				.when(new MockMvcWhen(builder, metaData))
				.then(new MockMvcThen(builder, metaData));

		ClassBodyBuilder bodyBuilder = ClassBodyBuilder.builder(builder)
				.field(new MessagingFields(builder, metaData))
				.methodBuilder(methodBuilder);

		GeneratedTestClass generatedTestClass = GeneratedTestClassBuilder.builder(builder)
				.classBodyBuilder(bodyBuilder)
				.metaData(new JavaClassMetaData(builder, metaData))
				.imports(new CustomImports(builder, metaData),
						new JsonImports(builder, metaData),
						new JUnit4Imports(builder, metaData),
						new Junit4IgnoreImports(builder, metaData),
						new JUnit4OrderImports(builder, metaData),
						new JsonPathImports(builder, metaData),
						new XmlImports(builder, metaData),
						new MessagingImports(builder, metaData),
						new MockMvcRestAssured3Imports(builder, metaData),
						new ExplicitRestAssured3Imports(builder, metaData))
				.staticImports(new DefaultStaticImports(builder, metaData),
						new MockMvcRestAssured3StaticImports(builder, metaData),
						new ExplicitRestAssured3StaticImports(builder, metaData),
						new CustomStaticImports(builder, metaData),
						new MessagingStaticImports(builder, metaData))
				.classAnnotations(new JUnit4OrderClassAnnotation(builder, metaData))
				.build();
		return generatedTestClass.asClassString();
	}

	@Override
	public String fileExtension(ContractVerifierConfigProperties properties) {
		return properties.getTestFramework().getClassExtension();
	}

}