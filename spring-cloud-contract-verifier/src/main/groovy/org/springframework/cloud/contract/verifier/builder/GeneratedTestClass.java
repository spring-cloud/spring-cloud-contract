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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.cloud.contract.verifier.config.ContractVerifierConfigProperties;
import org.springframework.cloud.contract.verifier.file.ContractMetadata;
import org.springframework.cloud.contract.verifier.file.SingleContractMetadata;

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

	final BlockBuilder blockBuilder;

	final GeneratedClassMetaData generatedClassMetaData;

	private GeneratedTestClassBuilder(BlockBuilder blockBuilder,
			GeneratedClassMetaData generatedClassMetaData) {
		this.blockBuilder = blockBuilder;
		this.generatedClassMetaData = generatedClassMetaData;
	}

	static GeneratedTestClassBuilder builder(BlockBuilder blockBuilder,
			GeneratedClassMetaData generatedClassMetaData) {
		return new GeneratedTestClassBuilder(blockBuilder, generatedClassMetaData);
	}

	GeneratedTestClassBuilder metaData(ClassMetaData metaData) {
		this.metaData.add(metaData);
		return this;
	}

	MetaDataBuilder metaData() {
		return new MetaDataBuilder(this);
	}

	GeneratedTestClassBuilder metaData(ClassMetaData... metaData) {
		return metaData(Arrays.asList(metaData));
	}

	GeneratedTestClassBuilder metaData(List<ClassMetaData> metaData) {
		this.metaData.addAll(metaData);
		return this;
	}

	ImportsBuilder imports() {
		return new ImportsBuilder(this);
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

	ClassAnnotationsBuilder classAnnotations() {
		return new ClassAnnotationsBuilder(this);
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
						"There is no matching class meta data"));
		classMetaData.setupLineEnding().setupLabelPrefix()
				// package com.example
				.packageDefinition();
		// \n
		this.blockBuilder.addEmptyLine();
		// import ... \n
		visit(this.imports);
		// import static ... \n
		visit(this.staticImports);
		// @Test ... \n
		visitWithNoEnding(this.annotations);
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
		visit(list, true);
	}

	void visitWithNoEnding(List<? extends Visitor> list) {
		visit(list, false);
	}

	private void visit(List<? extends Visitor> list, boolean addEnding) {
		List<Visitor> elements = list.stream().filter(Acceptor::accept)
				.collect(Collectors.toList());
		elements.forEach(OurCallable::call);
		if (addEnding) {
			this.blockBuilder.addEndingIfNotPresent();
		}
		if (!elements.isEmpty()) {
			this.blockBuilder.addEmptyLine();
		}
	}

}

class MetaDataBuilder {

	private final GeneratedTestClassBuilder parentBuilder;

	private final BlockBuilder builder;

	private final GeneratedClassMetaData metaData;

	MetaDataBuilder(GeneratedTestClassBuilder generatedTestClassBuilder) {
		this.parentBuilder = generatedTestClassBuilder;
		this.builder = generatedTestClassBuilder.blockBuilder;
		this.metaData = generatedTestClassBuilder.generatedClassMetaData;
	}

	MetaDataBuilder java() {
		this.parentBuilder.metaData(new JavaClassMetaData(this.builder, this.metaData));
		return this;
	}

	MetaDataBuilder groovy() {
		this.parentBuilder.metaData(new GroovyClassMetaData(this.builder, this.metaData));
		return this;
	}

	GeneratedTestClassBuilder build() {
		return this.parentBuilder;
	}

}

class ClassAnnotationsBuilder {

	private final GeneratedTestClassBuilder parentBuilder;

	private final BlockBuilder builder;

	private final GeneratedClassMetaData metaData;

	ClassAnnotationsBuilder(GeneratedTestClassBuilder generatedTestClassBuilder) {
		this.parentBuilder = generatedTestClassBuilder;
		this.builder = generatedTestClassBuilder.blockBuilder;
		this.metaData = generatedTestClassBuilder.generatedClassMetaData;
	}

	ClassAnnotationsBuilder jUnit4() {
		this.parentBuilder
				.classAnnotations(new JUnit4OrderClassAnnotation(builder, metaData));
		return this;
	}

	ClassAnnotationsBuilder jUnit5() {
		this.parentBuilder
				.classAnnotations(new JUnit5OrderClassAnnotation(builder, metaData));
		return this;
	}

	ClassAnnotationsBuilder spock() {
		this.parentBuilder
				.classAnnotations(new SpockOrderClassAnnotation(builder, metaData));
		return this;
	}

	GeneratedTestClassBuilder build() {
		return this.parentBuilder;
	}

}

class ImportsBuilder {

	private final GeneratedTestClassBuilder parentBuilder;

	private final BlockBuilder builder;

	private final GeneratedClassMetaData metaData;

	ImportsBuilder(GeneratedTestClassBuilder generatedTestClassBuilder) {
		this.parentBuilder = generatedTestClassBuilder;
		this.builder = generatedTestClassBuilder.blockBuilder;
		this.metaData = generatedTestClassBuilder.generatedClassMetaData;
	}

	ImportsBuilder defaultImports() {
		this.parentBuilder.imports(new DefaultImports(builder, metaData));
		this.parentBuilder.staticImports(new DefaultStaticImports(builder));
		return this;
	}

	ImportsBuilder custom() {
		this.parentBuilder.imports(new CustomImports(builder, metaData));
		this.parentBuilder.staticImports(new CustomStaticImports(builder, metaData));
		return this;
	}

	ImportsBuilder json() {
		this.parentBuilder.imports(new JsonImports(builder, metaData),
				new JsonPathImports(builder, metaData));
		this.parentBuilder.staticImports(new DefaultJsonStaticImports(builder, metaData));
		return this;
	}

	ImportsBuilder xml() {
		this.parentBuilder.imports(new XmlImports(builder, metaData));
		return this;
	}

	ImportsBuilder jUnit4() {
		this.parentBuilder.imports(new JUnit4Imports(builder, metaData),
				new JUnit4IgnoreImports(builder, metaData),
				new JUnit4OrderImports(builder, metaData));
		return this;
	}

	ImportsBuilder jUnit5() {
		this.parentBuilder.imports(new JUnit5Imports(builder, metaData),
				new JUnit5IgnoreImports(builder, metaData),
				new JUnit5OrderImports(builder, metaData));
		return this;
	}

	ImportsBuilder spock() {
		this.parentBuilder.imports(new SpockImports(builder, metaData),
				new SpockIgnoreImports(builder, metaData),
				new SpockOrderImports(builder, metaData));
		return this;
	}

	ImportsBuilder messaging() {
		this.parentBuilder.imports(new MessagingImports(builder, metaData));
		this.parentBuilder.staticImports(new MessagingStaticImports(builder, metaData));
		return this;
	}

	ImportsBuilder restAssured() {
		this.parentBuilder.imports(new MockMvcRestAssuredImports(builder, metaData),
				new ExplicitRestAssuredImports(builder, metaData),
				new WebTestClientRestAssuredImports(builder, metaData));
		this.parentBuilder.staticImports(
				new MockMvcRestAssuredStaticImports(builder, metaData),
				new ExplicitRestAssuredStaticImports(builder, metaData),
				new WebTestClientRestAssured3StaticImports(builder, metaData));
		return this;
	}

	ImportsBuilder jaxRs() {
		this.parentBuilder.imports(new JaxRsImports(builder, metaData));
		this.parentBuilder.staticImports(new JaxRsStaticImports(builder, metaData));
		return this;
	}

	GeneratedTestClassBuilder build() {
		return this.parentBuilder;
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

	final BlockBuilder blockBuilder;

	final GeneratedClassMetaData generatedClassMetaData;

	private ClassBodyBuilder(BlockBuilder blockBuilder,
			GeneratedClassMetaData generatedClassMetaData) {
		this.blockBuilder = blockBuilder;
		this.generatedClassMetaData = generatedClassMetaData;
	}

	static ClassBodyBuilder builder(BlockBuilder blockBuilder,
			GeneratedClassMetaData generatedClassMetaData) {
		return new ClassBodyBuilder(blockBuilder, generatedClassMetaData);
	}

	FieldBuilder field() {
		return new FieldBuilder(this);
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
		this.blockBuilder.inBraces(() -> {
			// @Rule ...
			visit(this.fields);
			// new line if fields added
			this.methodBuilder.build();
		});
		return this.blockBuilder;
	}

	void visit(List<? extends Visitor> list) {
		List<? extends Visitor> visitors = list.stream().filter(Acceptor::accept)
				.collect(Collectors.toList());
		Iterator<? extends Visitor> iterator = visitors.iterator();
		while (iterator.hasNext()) {
			Visitor visitor = iterator.next();
			visitor.call();
			this.blockBuilder.addEndingIfNotPresent();
			if (iterator.hasNext()) {
				this.blockBuilder.addEmptyLine();
			}
		}
	}

}

class FieldBuilder {

	private final ClassBodyBuilder parentBuilder;

	private final BlockBuilder builder;

	private final GeneratedClassMetaData metaData;

	FieldBuilder(ClassBodyBuilder parentBuilder) {
		this.parentBuilder = parentBuilder;
		this.builder = parentBuilder.blockBuilder;
		this.metaData = parentBuilder.generatedClassMetaData;
	}

	FieldBuilder messaging() {
		this.parentBuilder.field(new MessagingFields(this.builder, this.metaData));
		return this;
	}

	ClassBodyBuilder build() {
		return this.parentBuilder;
	}

}

interface ClassMetaData extends Acceptor {

	ClassMetaData setupLineEnding();

	ClassMetaData setupLabelPrefix();

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

	private List<MethodMetadata> methodMetadata = new LinkedList<>();

	private List<Given> givens = new LinkedList<>();

	private List<When> whens = new LinkedList<>();

	private List<Then> thens = new LinkedList<>();

	final GeneratedClassMetaData generatedClassMetaData;

	final BlockBuilder blockBuilder;

	private SingleMethodBuilder(BlockBuilder blockBuilder,
			GeneratedClassMetaData generatedClassMetaData) {
		this.blockBuilder = blockBuilder;
		this.generatedClassMetaData = generatedClassMetaData;
	}

	static SingleMethodBuilder builder(BlockBuilder blockBuilder,
			GeneratedClassMetaData generatedClassMetaData) {
		return new SingleMethodBuilder(blockBuilder, generatedClassMetaData);
	}

	SingleMethodBuilder methodAnnotation(MethodAnnotations methodAnnotations) {
		this.methodAnnotations.add(methodAnnotations);
		return this;
	}

	MethodAnnotationBuilder methodAnnotation() {
		return new MethodAnnotationBuilder(this);
	}

	SingleMethodBuilder methodAnnotation(MethodAnnotations... methodAnnotations) {
		this.methodAnnotations.addAll(Arrays.asList(methodAnnotations));
		return this;
	}

	MethodMetadataBuilder methodMetadata() {
		return new MethodMetadataBuilder(this);
	}

	SingleMethodBuilder methodMetadata(MethodMetadata... methodMetadata) {
		this.methodMetadata.addAll(Arrays.asList(methodMetadata));
		return this;
	}

	SingleMethodBuilder restAssured() {
		return given(
				new JavaRestAssuredGiven(this.blockBuilder, this.generatedClassMetaData))
						.given(new SpockRestAssuredGiven(this.blockBuilder,
								this.generatedClassMetaData))
						.when(new JavaRestAssuredWhen(this.blockBuilder,
								this.generatedClassMetaData))
						.when(new SpockRestAssuredWhen(this.blockBuilder,
								this.generatedClassMetaData))
						.then(new JavaRestAssuredThen(this.blockBuilder,
								this.generatedClassMetaData))
						.then(new SpockRestAssuredThen(this.blockBuilder,
								this.generatedClassMetaData));
	}

	SingleMethodBuilder jaxRs() {
		return given(new JaxRsGiven(this.generatedClassMetaData))
				.when(new JavaJaxRsWhen(this.blockBuilder, this.generatedClassMetaData))
				.when(new SpockJaxRsWhen(this.blockBuilder, this.generatedClassMetaData))
				.then(new JavaJaxRsThen(this.blockBuilder, this.generatedClassMetaData))
				.then(new SpockJaxRsThen(this.blockBuilder, this.generatedClassMetaData));
	}

	SingleMethodBuilder messaging() {
		return given(new MessagingGiven(this.blockBuilder, this.generatedClassMetaData))
				.when(new MessagingWhen(this.blockBuilder, this.generatedClassMetaData))
				.then(new JavaMessagingWithBodyThen(this.blockBuilder,
						this.generatedClassMetaData))
				.then(new SpockMessagingWithBodyThen(this.blockBuilder,
						this.generatedClassMetaData))
				.then(new SpockMessagingEmptyThen(this.blockBuilder,
						this.generatedClassMetaData));
	}

	SingleMethodBuilder given(Given... given) {
		this.givens.addAll(Arrays.asList(given));
		return this;
	}

	SingleMethodBuilder when(When... when) {
		this.whens.addAll(Arrays.asList(when));
		return this;
	}

	SingleMethodBuilder then(Then... then) {
		this.thens.addAll(Arrays.asList(then));
		return this;
	}

	/**
	 * Mutates the {@link BlockBuilder} to generate a methodBuilder
	 * @return block builder with contents of a single methodBuilder
	 */
	BlockBuilder build() {
		MethodMetadata methodMetadatum = pickMetadatum();
		// \n
		this.blockBuilder.addEmptyLine();
		this.generatedClassMetaData.toSingleContractMetadata().forEach(metaData -> {
			// @Test
			if (visit(this.methodAnnotations, metaData, false)) {
				this.blockBuilder.addEmptyLine();
			}
			// @formatter:off
			// public void validate_foo()
			this.blockBuilder.append(methodMetadatum::modifier)
					.appendWithSpace(methodMetadatum::returnType)
					.appendWithSpace(() -> methodMetadatum.name(metaData))
					.append("() throws Exception ");
			// (space) {
			this.blockBuilder.inBraces(() -> {
				// (indent) given
				if (visit(this.givens, metaData)) {
					this.blockBuilder.addEmptyLine();
				}
				// (indent) when
				visit(this.whens, metaData);
				this.blockBuilder.addEmptyLine();
				// (indent) then
				visit(this.thens, metaData);
			});
			this.blockBuilder.addEmptyLine();
			// }
		});
		// @formatter:on
		return this.blockBuilder;
	}

	private MethodMetadata pickMetadatum() {
		return this.methodMetadata.stream().filter(Acceptor::accept).findFirst()
				.orElseThrow(() -> new IllegalStateException(
						"No matching method metadata found"));
	}

	private boolean visit(List<? extends MethodVisitor> list,
			SingleContractMetadata metaData) {
		return visit(list, metaData, true);
	}

	private boolean visit(List<? extends MethodVisitor> list,
			SingleContractMetadata metaData, boolean addLineEnding) {
		List<? extends MethodVisitor> visitors = list.stream()
				.filter(o -> o.accept(metaData)).collect(Collectors.toList());
		Iterator<? extends MethodVisitor> iterator = visitors.iterator();
		while (iterator.hasNext()) {
			MethodVisitor visitor = iterator.next();
			visitor.apply(metaData);
			if (addLineEnding) {
				this.blockBuilder.addEndingIfNotPresent();
			}
			if (iterator.hasNext()) {
				this.blockBuilder.addEmptyLine();
			}
		}
		return !visitors.isEmpty();
	}

}

class MethodAnnotationBuilder {

	private final SingleMethodBuilder singleMethodBuilder;

	private final BlockBuilder builder;

	private final GeneratedClassMetaData metaData;

	MethodAnnotationBuilder(SingleMethodBuilder singleMethodBuilder) {
		this.singleMethodBuilder = singleMethodBuilder;
		this.builder = singleMethodBuilder.blockBuilder;
		this.metaData = singleMethodBuilder.generatedClassMetaData;
	}

	MethodAnnotationBuilder jUnit4() {
		this.singleMethodBuilder.methodAnnotation(
				new JUnit4MethodAnnotation(this.builder, this.metaData),
				new JUnit4IgnoreMethodAnnotation(this.builder, this.metaData));
		return this;
	}

	MethodAnnotationBuilder jUnit5() {
		this.singleMethodBuilder.methodAnnotation(
				new JUnit5MethodAnnotation(this.builder, this.metaData),
				new JUnit5IgnoreMethodAnnotation(this.builder, this.metaData));
		return this;
	}

	MethodAnnotationBuilder spock() {
		this.singleMethodBuilder.methodAnnotation(
				new SpockIgnoreMethodAnnotation(this.builder, this.metaData));
		return this;
	}

	SingleMethodBuilder build() {
		return this.singleMethodBuilder;
	}

}

class MethodMetadataBuilder {

	private final SingleMethodBuilder singleMethodBuilder;

	private final BlockBuilder builder;

	private final GeneratedClassMetaData metaData;

	MethodMetadataBuilder(SingleMethodBuilder singleMethodBuilder) {
		this.singleMethodBuilder = singleMethodBuilder;
		this.builder = singleMethodBuilder.blockBuilder;
		this.metaData = singleMethodBuilder.generatedClassMetaData;
	}

	MethodMetadataBuilder jUnit() {
		this.singleMethodBuilder
				.methodMetadata(new JUnitMethodMetadata(this.builder, this.metaData));
		return this;
	}

	MethodMetadataBuilder spock() {
		this.singleMethodBuilder
				.methodMetadata(new SpockMethodMetadata(this.builder, this.metaData));
		return this;
	}

	SingleMethodBuilder build() {
		return this.singleMethodBuilder;
	}

}

/**
 * Describes metadata of a single method.
 */
interface MethodMetadata extends Acceptor {

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
