package org.springframework.cloud.contract.verifier.builder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

import org.springframework.cloud.contract.verifier.config.ContractVerifierConfigProperties;
import org.springframework.cloud.contract.verifier.file.ContractMetadata;
import org.springframework.cloud.contract.verifier.util.ContentType;

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

// ClassAnnotation -> new JUnit4ClassAnnotation()

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
- ContractMetaData
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
class ContractMetaData {

	final ContentType contentType;

	final ContractVerifierConfigProperties configProperties;

	final Collection<ContractMetadata> listOfFiles;

	final String includedDirectoryRelativePath;

	final SingleTestGenerator.GeneratedClassData generatedClassData;

	ContractMetaData(ContractVerifierConfigProperties configProperties,
			Collection<ContractMetadata> listOfFiles,
			String includedDirectoryRelativePath,
			SingleTestGenerator.GeneratedClassData generatedClassData) {
		this.configProperties = configProperties;
		this.listOfFiles = listOfFiles;
		this.includedDirectoryRelativePath = includedDirectoryRelativePath;
		this.generatedClassData = generatedClassData;
		this.contentType = contentType();
	}

	private ContentType contentType() {
		// TODO: logic
		return this.contentType;
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

	private List<ClassMetaData> metaData = new ArrayList<>();

	private List<Imports> imports = new ArrayList<>();

	private List<ClassAnnotation> annotations = new ArrayList<>();

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
		visit(this.imports);
		// \n
		this.blockBuilder.addEmptyLine();
		visit(this.annotations);
		// public FooSpec extends Parent
		classMetaData.modifier().className().suffix().parentClass();
		// public FooSpec extends Parent {
		// (indent)
		// }
		this.blockBuilder.inBraces(() -> classBodyBuilder.build());
		return new GeneratedTestClass(this.blockBuilder);
	}

	void visit(List<? extends Visitor> list) {
		list.stream().filter(Acceptor::accept).forEach(Visitor::call);
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

	private List<Field> fields = new ArrayList<>();

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
		this.methodBuilder.build();
		return this.blockBuilder;
	}

	void visit(List<? extends Visitor> list) {
		// TODO: Consider setting new lines
		list.stream().filter(Acceptor::accept).forEach(Visitor::call);
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

	// @Rule
	Field annotations();

	// public
	Field modifier();

	// Block Builder
	Field field();

}

/**
 * Builds a single method body. Must be executed per contract.
 *
 * @author Olga Maciaszek-Sharma
 * @author Marcin Grzejszczak
 * @since 2.2.0
 */
class SingleMethodBuilder {

	private List<MethodAnnotations> methodAnnotations = new ArrayList<>();

	private MethodMetadata methodMetadata;

	private List<Given> givens = new ArrayList<>();

	private List<When> whens = new ArrayList<>();

	private List<Then> thens = new ArrayList<>();

	private List<ContractMetaData> contractMetaData = new LinkedList<>();

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

	SingleMethodBuilder methodMetadata(MethodMetadata methodMetadata) {
		this.methodMetadata = methodMetadata;
		return this;
	}

	SingleMethodBuilder contractMetaData(List<ContractMetaData> contractMetaData) {
		this.contractMetaData.addAll(contractMetaData);
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
		this.contractMetaData.forEach(metaData -> {
			// @Test
			visit(this.methodAnnotations, metaData);
			// \n
			this.blockBuilder.addEmptyLine();
			// public void validate_foo()
			this.methodMetadata.modifier().returnType().name();
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
		return this.blockBuilder;
	}

	private void visit(List<? extends MethodVisitor> list, ContractMetaData metaData) {
		// TODO: Consider setting new lines
		list.stream().filter(Acceptor::accept).forEach(o -> o.apply(contractMetaData));
	}

}

/**
 * Describes metadata of a single method.
 */
interface MethodMetadata {

	// validate_foo()
	MethodMetadata name();

	// public
	MethodMetadata modifier();

	// void
	MethodMetadata returnType();

}

interface Example {

	default void example_of_how_to_use_this() {
		BlockBuilder blockBuilder = new BlockBuilder(" ");
		List<ContractMetaData> contractMetaData = new ArrayList<>();

		SingleMethodBuilder methodBuilder = SingleMethodBuilder.builder(blockBuilder)
				// JUnitMethodAnnotation
				.methodAnnotation(null)
				// JavaMethodMetadata
				// SpockMethodMetadata
				.methodMetadata(null).contractMetaData(contractMetaData)
				// MockMvcGiven
				.given(null).given(null)
				// MockMvcWhen
				.when(null).when(null)
				// MockMvcThen
				.then(null).then(null);

		ClassBodyBuilder bodyBuilder = ClassBodyBuilder.builder(blockBuilder)
				// Junit5Field
				.field(null).field(null).field(null).methodBuilder(methodBuilder);

		GeneratedTestClass generatedTestClass = GeneratedTestClassBuilder
				.builder(blockBuilder).classBodyBuilder(bodyBuilder)
				.imports(new JsonImports(blockBuilder), new JUnit4Imports(blockBuilder))
				.classAnnotations(new JUnit4ClassAnnotation(blockBuilder)).build();

		// SingleTestGenerator requires a String
		String contentsOfASingleClass = generatedTestClass.asClassString();
	}

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
		extends Acceptor, Function<ContractMetaData, MethodVisitor<T>> {

}

interface OurCallable<T> {

	T call();

}

interface Acceptor {

	boolean accept();

}
