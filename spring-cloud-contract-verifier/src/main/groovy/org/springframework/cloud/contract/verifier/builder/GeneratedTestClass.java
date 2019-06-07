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

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.cloud.contract.verifier.config.ContractVerifierConfigProperties;
import org.springframework.cloud.contract.verifier.file.ContractMetadata;
import org.springframework.cloud.contract.verifier.file.SingleContractMetadata;

// JSON
// HTTP
// MOCK MVC [standalone, mockmvc, webclient]
// Scenario
// Custom rule

// JUNIT4

// ClassMetaData -> new JavaClassMetaData()

// Imports -> new JUnit4JsonMockMvcImports()
// Imports -> new Junit4Imports()
// Imports -> new JsonImports()
// Imports -> new MockMvcImports()

// ClassAnnotation -> new JUnit4OrderClassAnnotation()

// Field -> new

// MockMvcJsonMethodBuilder
// MockMvcBinaryMethodBuilder
// MockMvcFromFileMethodBuilder

/*

Variants:
- Content Type (Json, Xml, Binary)
-


 */

/*
Pojos:
- GeneratedClassMetaData
- GeneratedTestClass

Builders
- GeneratedTestClassBuilder (package, imports, class annotation, delegates to class body)
- ClassBodyBuilder (class body, fields, delegates to method)
- SingleMethodBuilder (a single method)

 */

/**
 * Contents of the generated test.
 *
 * @author Olga Maciaszek-Sharma
 * @author Marcin Grzejszczak
 * @since 2.2.0
 */
public class GeneratedTestClass {

	final BlockBuilder blockBuilder;

	GeneratedTestClass(BlockBuilder blockBuilder) {
		this.blockBuilder = blockBuilder;
	}

	public String asClassString() {
		return this.blockBuilder.toString();
	}

}

/**
 * All meta data required to generate a test class.
 *
 * @author Olga Maciaszek-Sharma
 * @author Marcin Grzejszczak
 * @since 2.2.0
 */
class GeneratedClassMetaData {

	final ContractVerifierConfigProperties configProperties;

	final Collection<ContractMetadata> listOfFiles;

	final String includedDirectoryRelativePath;

	final SingleTestGenerator.GeneratedClassData generatedClassData;

	GeneratedClassMetaData(ContractVerifierConfigProperties configProperties,
			Collection<ContractMetadata> listOfFiles,
			String includedDirectoryRelativePath,
			SingleTestGenerator.GeneratedClassData generatedClassData) {
		this.configProperties = configProperties;
		this.listOfFiles = listOfFiles;
		this.includedDirectoryRelativePath = includedDirectoryRelativePath;
		this.generatedClassData = generatedClassData;
	}

	Collection<SingleContractMetadata> toSingleContractMetadata() {
		return this.listOfFiles.stream()
				.flatMap(metadata -> metadata.getConvertedContractWithMetadata().stream())
				.collect(Collectors.toList());
	}

	boolean isAnyJson() {
		return toSingleContractMetadata().stream()
				.anyMatch(SingleContractMetadata::isJson);
	}

	boolean isAnyIgnored() {
		return toSingleContractMetadata().stream()
				.anyMatch(SingleContractMetadata::isIgnored);
	}

	boolean isAnyXml() {
		return toSingleContractMetadata().stream()
				.anyMatch(SingleContractMetadata::isXml);
	}

	boolean isAnyHttp() {
		return toSingleContractMetadata().stream()
				.anyMatch(SingleContractMetadata::isHttp);
	}

	boolean isAnyMessaging() {
		return toSingleContractMetadata().stream()
				.anyMatch(SingleContractMetadata::isMessaging);
	}

}

/**
 * A generated test class consists of the class meta data (e.g. packages, imports) fields
 * and methods. The latter are generated via the {@link ClassBodyBuilder}.
 *
 * @author Olga Maciaszek-Sharma
 * @author Marcin Grzejszczak
 * @since 2.2.0
 */
class GeneratedTestClassBuilder {

	private List<ClassMetaData> metaData = new LinkedList<>();

	private List<Imports> imports = new LinkedList<>();

	private List<Imports> staticImports = new LinkedList<>();

	private List<ClassAnnotation> annotations = new LinkedList<>();

	private ClassBodyBuilder classBodyBuilder;

	private final BlockBuilder blockBuilder;

	private GeneratedTestClassBuilder(BlockBuilder blockBuilder) {
		this.blockBuilder = blockBuilder;
	}

	static GeneratedTestClassBuilder builder(BlockBuilder blockBuilder) {
		return new GeneratedTestClassBuilder(blockBuilder);
	}

	GeneratedTestClassBuilder metaData(ClassMetaData metaData) {
		this.metaData.add(metaData);
		return this;
	}

	GeneratedTestClassBuilder imports(Imports imports) {
		this.imports.add(imports);
		return this;
	}

	GeneratedTestClassBuilder imports(Imports... imports) {
		return imports(Arrays.asList(imports));
	}

	GeneratedTestClassBuilder imports(List<Imports> imports) {
		this.imports.addAll(imports);
		return this;
	}

	GeneratedTestClassBuilder staticImports(Imports imports) {
		this.staticImports.add(imports);
		return this;
	}

	GeneratedTestClassBuilder staticImports(Imports... imports) {
		return staticImports(Arrays.asList(imports));
	}

	GeneratedTestClassBuilder staticImports(List<Imports> imports) {
		this.staticImports.addAll(imports);
		return this;
	}

	GeneratedTestClassBuilder classAnnotations(ClassAnnotation... annotations) {
		List<ClassAnnotation> classAnnotations = Arrays.asList(annotations);
		this.annotations.addAll(classAnnotations);
		return this;
	}

	GeneratedTestClassBuilder classBodyBuilder(ClassBodyBuilder classBodyBuilder) {
		this.classBodyBuilder = classBodyBuilder;
		return this;
	}

	/**
	 * From a matching {@link ClassMetaData} given the present input data, builds a
	 * generated test class.
	 * @return generated test class
	 */
	GeneratedTestClass build() {
		// picks a matching class meta data
		ClassMetaData classMetaData = this.metaData.stream().filter(Acceptor::accept)
				.findFirst().orElseThrow(() -> new IllegalStateException(
						"There is no matching meta data"));
		// package com.example
		classMetaData.setupLineEnding().packageDefinition();
		// \n
		this.blockBuilder.addEmptyLine();
		// import ... \n
		visit(this.imports);
		// import static ... \n
		visit(this.staticImports);
		// @Test ... \n
		visit(this.annotations);
		// @formatter:off
		// public
		this.blockBuilder.append(classMetaData::modifier)
				// class
				.appendWithSpace("class")
				// Foo
				.appendWithSpace(classMetaData::className)
				// Spec
				.append(classMetaData::suffix)
				// extends Parent
				.appendWithSpace(classMetaData::parentClass);
		// public class FooSpec extends Parent
		// @formatter:on
		this.classBodyBuilder.build();
		return new GeneratedTestClass(this.blockBuilder);
	}

	void visit(List<? extends Visitor> list) {
		List<Visitor> elements = list.stream().filter(Acceptor::accept)
				.collect(Collectors.toList());
		elements.forEach(OurCallable::call);
		if (!elements.isEmpty()) {
			this.blockBuilder.addEmptyLine();
		}
	}

}

/**
 * Builds the body of the class. Sets fields, methods.
 *
 * @author Olga Maciaszek-Sharma
 * @author Marcin Grzejszczak
 * @since 2.2.0
 */
class ClassBodyBuilder {

	private List<Field> fields = new LinkedList<>();

	private SingleMethodBuilder methodBuilder;

	private final BlockBuilder blockBuilder;

	private ClassBodyBuilder(BlockBuilder blockBuilder) {
		this.blockBuilder = blockBuilder;
	}

	static ClassBodyBuilder builder(BlockBuilder blockBuilder) {
		return new ClassBodyBuilder(blockBuilder);
	}

	ClassBodyBuilder field(Field field) {
		this.fields.add(field);
		return this;
	}

	ClassBodyBuilder methodBuilder(SingleMethodBuilder methodBuilder) {
		this.methodBuilder = methodBuilder;
		return this;
	}

	/**
	 * Mutates the {@link BlockBuilder} to generate methods and fields.
	 * @return block builder with contents of built methods
	 */
	BlockBuilder build() {
		// @Rule ...
		visit(this.fields);
		// new line if fields added
		this.blockBuilder.inBraces(() -> this.methodBuilder.build());
		return this.blockBuilder;
	}

	void visit(List<? extends Visitor> list) {
		list.stream().filter(Acceptor::accept).forEach(visitor -> {
			visitor.call();
			this.blockBuilder.addEmptyLine();
		});
	}

}

interface ClassMetaData extends Acceptor {

	ClassMetaData setupLineEnding();

	ClassMetaData packageDefinition();

	ClassMetaData modifier();

	ClassMetaData suffix();

	ClassMetaData parentClass();

	ClassMetaData className();

}

interface ClassAnnotation extends Visitor<ClassAnnotation> {

}

interface Imports extends Visitor<Imports> {

}

interface Field extends Visitor<Field> {

}

/**
 * Builds a single method body. Must be executed per contract.
 *
 * @author Olga Maciaszek-Sharma
 * @author Marcin Grzejszczak
 * @since 2.2.0
 */
class SingleMethodBuilder {

	private List<MethodAnnotations> methodAnnotations = new LinkedList<>();

	private MethodMetadata methodMetadata;

	private List<Given> givens = new LinkedList<>();

	private List<When> whens = new LinkedList<>();

	private List<Then> thens = new LinkedList<>();

	private GeneratedClassMetaData generatedClassMetaData;

	private final BlockBuilder blockBuilder;

	private SingleMethodBuilder(BlockBuilder blockBuilder) {
		this.blockBuilder = blockBuilder;
	}

	static SingleMethodBuilder builder(BlockBuilder blockBuilder) {
		return new SingleMethodBuilder(blockBuilder);
	}

	SingleMethodBuilder methodAnnotation(MethodAnnotations methodAnnotations) {
		this.methodAnnotations.add(methodAnnotations);
		return this;
	}

	SingleMethodBuilder methodAnnotation(MethodAnnotations... methodAnnotations) {
		this.methodAnnotations.addAll(Arrays.asList(methodAnnotations));
		return this;
	}

	SingleMethodBuilder methodMetadata(MethodMetadata methodMetadata) {
		this.methodMetadata = methodMetadata;
		return this;
	}

	SingleMethodBuilder contractMetaData(GeneratedClassMetaData generatedClassMetaData) {
		this.generatedClassMetaData = generatedClassMetaData;
		return this;
	}

	SingleMethodBuilder given(Given given) {
		this.givens.add(given);
		return this;
	}

	SingleMethodBuilder when(When when) {
		this.whens.add(when);
		return this;
	}

	SingleMethodBuilder then(Then then) {
		this.thens.add(then);
		return this;
	}

	/**
	 * Mutates the {@link BlockBuilder} to generate a methodBuilder
	 * @return block builder with contents of a single methodBuilder
	 */
	BlockBuilder build() {
		// \n
		this.blockBuilder.addEmptyLine();
		this.generatedClassMetaData.toSingleContractMetadata().forEach(metaData -> {
			// @Test
			visit(this.methodAnnotations, metaData);
			// @formatter:off
			// public void validate_foo()
			this.blockBuilder.append(methodMetadata::modifier)
					.appendWithSpace(methodMetadata::returnType)
					.appendWithSpace(() -> methodMetadata.name(metaData))
					.append("() throws Exception ");
			// (space) {
			this.blockBuilder.inBraces(() -> {
				// (indent) given
				visit(this.givens, metaData);
				// (indent) when
				visit(this.whens, metaData);
				// (indent) then
				visit(this.thens, metaData);
			});
			// }
		});
		// @formatter:on
		return this.blockBuilder;
	}

	private void visit(List<? extends MethodVisitor> list,
			SingleContractMetadata metaData) {
		list.stream().filter(o -> o.accept(metaData)).forEach(visitor -> {
			visitor.apply(metaData);
			this.blockBuilder.addEmptyLine();
		});
	}

}

/**
 * Describes metadata of a single method.
 */
interface MethodMetadata {

	// validate_foo()
	MethodMetadata name(SingleContractMetadata metadata);

	// public
	MethodMetadata modifier();

	// void
	MethodMetadata returnType();

}

interface MethodAnnotations extends MethodVisitor<MethodAnnotations> {

}

interface Given extends MethodVisitor<Given> {

}

interface When extends MethodVisitor<When> {

}

interface Then extends MethodVisitor<Then> {

}

interface Visitor<T> extends Acceptor, OurCallable<T> {

}

interface MethodVisitor<T>
		extends MethodAcceptor, Function<SingleContractMetadata, MethodVisitor<T>> {

}

interface OurCallable<T> {

	T call();

}

interface Acceptor {

	boolean accept();

}

interface MethodAcceptor {

	boolean accept(SingleContractMetadata metadata);

}
