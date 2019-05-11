package org.springframework.cloud.contract.verifier.builder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.springframework.cloud.contract.spec.Contract;
import org.springframework.cloud.contract.verifier.config.ContractVerifierConfigProperties;
import org.springframework.cloud.contract.verifier.file.ContractMetadata;
import org.springframework.cloud.contract.verifier.util.ContentType;

public class BuiltClass {

	final BlockBuilder blockBuilder;

	BuiltClass(BlockBuilder blockBuilder) {
		this.blockBuilder = blockBuilder;
	}

	public String asFile() {
		return this.blockBuilder.toString();
	}

}

class ContractMetaData {

	final Contract contract;

	final ContentType contentType;

	final ContractVerifierConfigProperties configProperties;

	final Collection<ContractMetadata> listOfFiles;

	final String includedDirectoryRelativePath;

	final SingleTestGenerator.GeneratedClassData generatedClassData;

	ContractMetaData(Contract contract,
			ContractVerifierConfigProperties configProperties, Collection<ContractMetadata> listOfFiles, String includedDirectoryRelativePath, SingleTestGenerator.GeneratedClassData generatedClassData) {
		this.contract = contract;
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

class OurClassMetaDataBuilder {

	private List<ClassMetaData> metaData = new ArrayList<>();

	private List<Imports> imports = new ArrayList<>();

	private List<ClassAnnotation> annotations = new ArrayList<>();

	private ClassBodyBuilder classBodyBuilder;

	private final BlockBuilder blockBuilder;

	private OurClassMetaDataBuilder(BlockBuilder blockBuilder) {
		this.blockBuilder = blockBuilder;
	}

	static OurClassMetaDataBuilder builder(BlockBuilder blockBuilder) {
		return new OurClassMetaDataBuilder(blockBuilder);
	}

	OurClassMetaDataBuilder metaData(ClassMetaData metaData) {
		this.metaData.add(metaData);
		return this;
	}

	OurClassMetaDataBuilder imports(Imports imports) {
		this.imports.add(imports);
		return this;
	}

	OurClassMetaDataBuilder imports(List<Imports> imports) {
		this.imports.addAll(imports);
		return this;
	}

	OurClassMetaDataBuilder classAnnotations(ClassAnnotation... annotations) {
		List<ClassAnnotation> classAnnotations = Arrays.asList(annotations);
		this.annotations.addAll(classAnnotations);
		return this;
	}

	OurClassMetaDataBuilder classBodyBuilder(ClassBodyBuilder classBodyBuilder) {
		this.classBodyBuilder = classBodyBuilder;
		return this;
	}

	BuiltClass build() {
		// com.example
		ClassMetaData classMetaData = this.metaData.stream().filter(Acceptor::accept).findFirst()
				.orElseThrow(() -> new IllegalStateException("There is no matching meta data"));
		classMetaData
				.setupLineEnding()
				.packageDefinition();
		// \n
		this.blockBuilder.addEmptyLine();
		visit(this.imports);
		// \n
		this.blockBuilder.addEmptyLine();
		visit(this.annotations);
		// public FooSpec extends Parent
		classMetaData.modifier().className().suffix().parentClass();
		// public FooSpec extends Parent {
		//   (indent)
		// }
		this.blockBuilder.inBraces(() -> classBodyBuilder.build());
		return new BuiltClass(this.blockBuilder);
	}

	void visit(List<? extends Visitor> list) {
		list.stream().filter(Acceptor::accept).forEach(Visitor::call);
	}

}

class ClassBodyBuilder {

	private List<Field> fields = new ArrayList<>();

	private List<Method> methods = new ArrayList<>();

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

	ClassBodyBuilder method(Method method) {
		this.methods.add(method);
		return this;
	}

	BlockBuilder build() {
		// in this method we will mutate block builder
		// and pass it to the built class

		return this.blockBuilder;
	}

}

interface ClassMetaData extends Acceptor {

	default void asidj() {
		BlockBuilder blockBuilder = new BlockBuilder(" ");
		ClassBodyBuilder bodyBuilder = ClassBodyBuilder.builder(blockBuilder);
		BuiltClass builtClass = OurClassMetaDataBuilder.builder(blockBuilder)
				.classBodyBuilder(bodyBuilder)
				.build();
	}

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

interface Field {

	Field annotations();

	Field modifier();

	Field field();

}

interface Given extends Visitor<Method> {

}

interface MethodAnnotations extends Visitor<Method> {

}

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

interface MyMethodBuilder extends Visitor<Method> {

	MyMethodBuilder annotations(MethodAnnotations annotations);

	// JunitMethodMetadata
	MyMethodBuilder modifier(MethodMetadata methodMetadata);

	// MockMvcJsonGiven
	MyMethodBuilder given(Given given);

	// MockMvcWhen
	MyMethodBuilder when(When when);

	// JsonPayloadThen
	MyMethodBuilder then(Then then);

	// na call
	/*
	 * method .annotations() .modifier() .given() .when() .then()
	 */

	default void foo() {
		new MyMethodBuilder() {

			@Override
			public boolean accept() {
				return false;
			}

			@Override
			public Method call() {
				return null;
			}

			@Override
			public MyMethodBuilder annotations(MethodAnnotations annotations) {
				return null;
			}

			@Override
			public MyMethodBuilder modifier(MethodMetadata methodMetadata) {
				return null;
			}

			@Override
			public MyMethodBuilder given(Given given) {
				return null;
			}

			@Override
			public MyMethodBuilder when(When when) {
				return null;
			}

			@Override
			public MyMethodBuilder then(Then then) {
				return null;
			}
		};
	}

}

interface MethodMetadata {

	MethodMetadata name();

	MethodMetadata modifier();

}

interface Method {

	Method name();

	Method annotations();

	Method modifier();

	Method given();

	Method when();

	Method then();

	default void foo() {

		BlockBuilder blockBuilder = new BlockBuilder("");
		// opis tego co mozemy zrobic
		BuiltClass builtClass = ClassBodyBuilder.builder(blockBuilder)
				.metaData(new JavaClassMetaData(blockBuilder, contractMetaData))
				.imports(new JUnit4JsonMockMvcImports(blockBuilder))
				.imports(new JUnit5JsonMockMvcImports(blockBuilder))
				.classAnnotations(new JUnit4ClassAnnotation(blockBuilder)).field(null)
				.field(null).method(null).method(null).method(null).build();

		// w bebechu builda
		builtClass.methods.forEach(
				method -> method.annotations().modifier().name().given().when().then());
		;
	}

}

interface Assertions extends Visitor<Then> {

}

interface Then extends Visitor<Then> {

	Assertions assertions();

}

interface When extends Visitor<Method> {

}

interface Visitor<T> extends Acceptor, OurCallable<T> {

}

interface OurCallable<T> {

	T call();

}

interface Acceptor {

	boolean accept();

}
