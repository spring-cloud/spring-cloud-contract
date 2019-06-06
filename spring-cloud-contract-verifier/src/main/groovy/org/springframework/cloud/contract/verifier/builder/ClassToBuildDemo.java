package org.springframework.cloud.contract.verifier.builder;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.cloud.contract.spec.Contract;
import org.springframework.cloud.contract.verifier.config.ContractVerifierConfigProperties;
import org.springframework.cloud.contract.verifier.config.TestFramework;
import org.springframework.cloud.contract.verifier.file.ContractMetadata;
import org.springframework.cloud.contract.verifier.file.SingleContractMetadata;
import org.springframework.cloud.contract.verifier.util.NamesUtil;
import org.springframework.util.StringUtils;

import static org.springframework.cloud.contract.verifier.util.NamesUtil.capitalize;

class ClassToBuildDemo {

}

class CustomImports implements Imports {

	private final BlockBuilder blockBuilder;

	private final GeneratedClassMetaData generatedClassMetaData;

	CustomImports(BlockBuilder blockBuilder,
			GeneratedClassMetaData generatedClassMetaData) {
		this.blockBuilder = blockBuilder;
		this.generatedClassMetaData = generatedClassMetaData;
	}

	@Override
	public Imports call() {
		Arrays.stream(this.generatedClassMetaData.configProperties.getImports())
				.forEach(s -> this.blockBuilder.addLineWithEnding("import " + s));
		return this;
	}

	@Override
	public boolean accept() {
		return this.generatedClassMetaData.configProperties.getImports().length > 0;
	}

}

class Junit4IgnoreImports implements Imports {

	private final BlockBuilder blockBuilder;

	private final GeneratedClassMetaData generatedClassMetaData;

	Junit4IgnoreImports(BlockBuilder blockBuilder,
			GeneratedClassMetaData generatedClassMetaData) {
		this.blockBuilder = blockBuilder;
		this.generatedClassMetaData = generatedClassMetaData;
	}

	@Override
	public Imports call() {
		this.blockBuilder.addLineWithEnding("import org.junit.Ignore");
		return this;
	}

	@Override
	public boolean accept() {
		return this.generatedClassMetaData.listOfFiles.stream()
				.anyMatch(metadata -> metadata.isIgnored()
						|| metadata.getConvertedContractWithMetadata().stream()
								.anyMatch(SingleContractMetadata::isIgnored));
	}

}

class CustomStaticImports implements Imports {

	private final BlockBuilder blockBuilder;

	private final GeneratedClassMetaData generatedClassMetaData;

	CustomStaticImports(BlockBuilder blockBuilder,
			GeneratedClassMetaData generatedClassMetaData) {
		this.blockBuilder = blockBuilder;
		this.generatedClassMetaData = generatedClassMetaData;
	}

	@Override
	public Imports call() {
		Arrays.stream(this.generatedClassMetaData.configProperties.getStaticImports())
				.forEach(s -> this.blockBuilder.addLineWithEnding("import static " + s));
		return this;
	}

	@Override
	public boolean accept() {
		return this.generatedClassMetaData.configProperties.getStaticImports().length > 0;
	}

}

class JUnit4Imports implements Imports {

	private final BlockBuilder blockBuilder;

	private final GeneratedClassMetaData generatedClassMetaData;

	private static final String[] IMPORTS = { "org.junit.Test", "org.junit.Rule" };

	JUnit4Imports(BlockBuilder blockBuilder,
			GeneratedClassMetaData generatedClassMetaData) {
		this.blockBuilder = blockBuilder;
		this.generatedClassMetaData = generatedClassMetaData;
	}

	@Override
	public Imports call() {
		Arrays.stream(IMPORTS)
				.forEach(s -> this.blockBuilder.addLineWithEnding("import " + s));
		return this;
	}

	@Override
	public boolean accept() {
		return this.generatedClassMetaData.configProperties
				.getTestFramework() == TestFramework.JUNIT;
	}

}

class DefaultStaticImports implements Imports {

	private final BlockBuilder blockBuilder;

	private final GeneratedClassMetaData generatedClassMetaData;

	private static final String[] IMPORTS = {
			"com.toomuchcoding.jsonassert.JsonAssertion.assertThatJson" };

	DefaultStaticImports(BlockBuilder blockBuilder,
			GeneratedClassMetaData generatedClassMetaData) {
		this.blockBuilder = blockBuilder;
		this.generatedClassMetaData = generatedClassMetaData;
	}

	@Override
	public Imports call() {
		Arrays.stream(IMPORTS)
				.forEach(s -> this.blockBuilder.addLineWithEnding("import static " + s));
		return this;
	}

	@Override
	public boolean accept() {
		return this.generatedClassMetaData.configProperties
				.getTestFramework() == TestFramework.JUNIT;
	}

}

class JUnit4OrderImports implements Imports {

	private final BlockBuilder blockBuilder;

	private final GeneratedClassMetaData generatedClassMetaData;

	private static final String[] IMPORTS = { "org.junit.FixMethodOrder",
			"org.junit.runners.MethodSorters" };

	JUnit4OrderImports(BlockBuilder blockBuilder,
			GeneratedClassMetaData generatedClassMetaData) {
		this.blockBuilder = blockBuilder;
		this.generatedClassMetaData = generatedClassMetaData;
	}

	@Override
	public Imports call() {
		Arrays.stream(IMPORTS)
				.forEach(s -> this.blockBuilder.addLineWithEnding("import " + s));
		return this;
	}

	@Override
	public boolean accept() {
		return this.generatedClassMetaData.listOfFiles.stream()
				.anyMatch(meta -> meta.getOrder() != null);
	}

}

class JsonImports implements Imports {

	private final BlockBuilder blockBuilder;

	private final GeneratedClassMetaData generatedClassMetaData;

	private static final String[] IMPORTS = { "org.junit.FixMethodOrder",
			"org.junit.runners.MethodSorters" };

	JsonImports(BlockBuilder blockBuilder,
			GeneratedClassMetaData generatedClassMetaData) {
		this.blockBuilder = blockBuilder;
		this.generatedClassMetaData = generatedClassMetaData;
	}

	@Override
	public Imports call() {
		Arrays.stream(IMPORTS)
				.forEach(s -> this.blockBuilder.addLineWithEnding("import " + s));
		return this;
	}

	@Override
	public boolean accept() {
		return this.generatedClassMetaData.listOfFiles.stream()
				.anyMatch(metadata -> metadata.getConvertedContractWithMetadata().stream()
						.anyMatch(SingleContractMetadata::isJson));
	}

}

class XmlImports implements Imports {

	private final BlockBuilder blockBuilder;

	private final GeneratedClassMetaData generatedClassMetaData;

	private static final String[] IMPORTS = { "javax.xml.parsers.DocumentBuilder",
			"javax.xml.parsers.DocumentBuilderFactory", "org.w3c.dom.Document",
			"org.xml.sax.InputSource", "java.io.StringReader" };

	XmlImports(BlockBuilder blockBuilder, GeneratedClassMetaData generatedClassMetaData) {
		this.blockBuilder = blockBuilder;
		this.generatedClassMetaData = generatedClassMetaData;
	}

	@Override
	public Imports call() {
		Arrays.stream(IMPORTS)
				.forEach(s -> this.blockBuilder.addLineWithEnding("import " + s));
		return this;
	}

	@Override
	public boolean accept() {
		return this.generatedClassMetaData.listOfFiles.stream()
				.anyMatch(metadata -> metadata.getConvertedContractWithMetadata().stream()
						.anyMatch(SingleContractMetadata::isXml));
	}

}

class MessagingImports implements Imports {

	private final BlockBuilder blockBuilder;

	private final GeneratedClassMetaData generatedClassMetaData;

	private static final String[] IMPORTS = { "javax.inject.Inject",
			"org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierObjectMapper",
			"org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierMessage",
			"org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierMessaging" };

	MessagingImports(BlockBuilder blockBuilder,
			GeneratedClassMetaData generatedClassMetaData) {
		this.blockBuilder = blockBuilder;
		this.generatedClassMetaData = generatedClassMetaData;
	}

	@Override
	public Imports call() {
		Arrays.stream(IMPORTS)
				.forEach(s -> this.blockBuilder.addLineWithEnding("import " + s));
		return this;
	}

	@Override
	public boolean accept() {
		return this.generatedClassMetaData.listOfFiles.stream()
				.anyMatch(metadata -> metadata.getConvertedContractWithMetadata().stream()
						.anyMatch(SingleContractMetadata::isMessaging));
	}

}

class MessagingFields implements Field {

	private final BlockBuilder blockBuilder;

	private final GeneratedClassMetaData generatedClassMetaData;

	private static final String[] FIELDS = {
			"@Inject ContractVerifierMessaging contractVerifierMessaging",
			"@Inject ContractVerifierObjectMapper contractVerifierObjectMapper" };

	MessagingFields(BlockBuilder blockBuilder,
			GeneratedClassMetaData generatedClassMetaData) {
		this.blockBuilder = blockBuilder;
		this.generatedClassMetaData = generatedClassMetaData;
	}

	@Override
	public Field call() {
		Arrays.stream(FIELDS).forEach(this.blockBuilder::addLineWithEnding);
		return this;
	}

	@Override
	public boolean accept() {
		return this.generatedClassMetaData.listOfFiles.stream()
				.anyMatch(metadata -> metadata.getConvertedContractWithMetadata().stream()
						.anyMatch(SingleContractMetadata::isMessaging));
	}

}

class MessagingStaticImports implements Imports {

	private final BlockBuilder blockBuilder;

	private final GeneratedClassMetaData generatedClassMetaData;

	private static final String[] IMPORTS = {
			"org.springframework.cloud.contract.verifier.messaging.util.ContractVerifierMessagingUtil.headers",
			"org.springframework.cloud.contract.verifier.util.ContractVerifierUtil.fileToBytes" };

	MessagingStaticImports(BlockBuilder blockBuilder,
			GeneratedClassMetaData generatedClassMetaData) {
		this.blockBuilder = blockBuilder;
		this.generatedClassMetaData = generatedClassMetaData;
	}

	@Override
	public Imports call() {
		Arrays.stream(IMPORTS)
				.forEach(s -> this.blockBuilder.addLineWithEnding("import static " + s));
		return this;
	}

	@Override
	public boolean accept() {
		return this.generatedClassMetaData.listOfFiles.stream()
				.anyMatch(metadata -> metadata.getConvertedContractWithMetadata().stream()
						.anyMatch(SingleContractMetadata::isMessaging));
	}

}

class RestAssured3MockMvcImports implements Imports {

	private final BlockBuilder blockBuilder;

	private final GeneratedClassMetaData generatedClassMetaData;

	private static final String[] IMPORTS = {
			"io.restassured.module.mockmvc.RestAssuredMockMvc.*",
			"io.restassured.module.mockmvc.specification.MockMvcRequestSpecification",
			"io.restassured.response.ResponseOptions" };

	RestAssured3MockMvcImports(BlockBuilder blockBuilder,
			GeneratedClassMetaData generatedClassMetaData) {
		this.blockBuilder = blockBuilder;
		this.generatedClassMetaData = generatedClassMetaData;
	}

	@Override
	public Imports call() {
		Arrays.stream(IMPORTS)
				.forEach(s -> this.blockBuilder.addLineWithEnding("import " + s));
		return this;
	}

	@Override
	public boolean accept() {
		return this.generatedClassMetaData.configProperties
				.getTestFramework() == TestFramework.JUNIT
				|| this.generatedClassMetaData.configProperties
						.getTestFramework() == TestFramework.JUNIT5;
	}

}

class JsonPathImports implements Imports {

	private final BlockBuilder blockBuilder;

	private final GeneratedClassMetaData generatedClassMetaData;

	private static final String[] IMPORTS = { "com.jayway.jsonpath.DocumentContext",
			"com.jayway.jsonpath.JsonPath" };

	JsonPathImports(BlockBuilder blockBuilder,
			GeneratedClassMetaData generatedClassMetaData) {
		this.blockBuilder = blockBuilder;
		this.generatedClassMetaData = generatedClassMetaData;
	}

	@Override
	public Imports call() {
		Arrays.stream(IMPORTS)
				.forEach(s -> this.blockBuilder.addLineWithEnding("import " + s));
		return this;
	}

	@Override
	public boolean accept() {
		return this.generatedClassMetaData.listOfFiles.stream()
				.anyMatch(metadata -> metadata.getConvertedContractWithMetadata().stream()
						.anyMatch(SingleContractMetadata::isJson));
	}

}

class JavaClassMetaData implements ClassMetaData {

	private final BlockBuilder blockBuilder;

	private final BaseClassProvider baseClassProvider = new BaseClassProvider();

	private final GeneratedClassMetaData generatedClassMetaData;

	JavaClassMetaData(BlockBuilder blockBuilder,
			GeneratedClassMetaData generatedClassMetaData) {
		this.blockBuilder = blockBuilder;
		this.generatedClassMetaData = generatedClassMetaData;
	}

	@Override
	public ClassMetaData packageDefinition() {
		this.blockBuilder.addLineWithEnding(
				"package " + this.generatedClassMetaData.generatedClassData.classPackage);
		return this;
	}

	@Override
	public ClassMetaData modifier() {
		this.blockBuilder.append("public");
		return this;
	}

	@Override
	public ClassMetaData suffix() {
		String suffix = StringUtils.hasText(
				this.generatedClassMetaData.configProperties.getNameSuffixForTests())
						? this.generatedClassMetaData.configProperties
								.getNameSuffixForTests()
						: "Tests";
		this.blockBuilder.addAtTheEnd(suffix);
		return this;
	}

	@Override
	public ClassMetaData setupLineEnding() {
		this.blockBuilder.lineEnding(";");
		return this;
	}

	@Override
	public ClassMetaData parentClass() {
		ContractVerifierConfigProperties properties = this.generatedClassMetaData.configProperties;
		String includedDirectoryRelativePath = this.generatedClassMetaData.includedDirectoryRelativePath;
		String baseClass = this.baseClassProvider.retrieveBaseClass(properties,
				includedDirectoryRelativePath);
		if (StringUtils.hasText(baseClass)) {
			this.blockBuilder.append("extends ").append(baseClass).append(" ");
		}
		return this;
	}

	@Override
	public ClassMetaData className() {
		String className = capitalize(
				this.generatedClassMetaData.generatedClassData.className);
		this.blockBuilder.addAtTheEnd(className);
		return this;
	}

	@Override
	public boolean accept() {
		return this.generatedClassMetaData.configProperties
				.getTestFramework() == TestFramework.JUNIT
				|| this.generatedClassMetaData.configProperties
						.getTestFramework() == TestFramework.JUNIT5;
	}

}

class BaseClassProvider {

	private static final Log log = LogFactory.getLog(BaseClassProvider.class);

	private static final String SEPARATOR = "_REPLACEME_";

	String retrieveBaseClass(ContractVerifierConfigProperties properties,
			String includedDirectoryRelativePath) {
		String contractPathAsPackage = includedDirectoryRelativePath
				.replace(File.separator, ".");
		String contractPackage = includedDirectoryRelativePath.replace(File.separator,
				SEPARATOR);
		// package mapping takes super precedence
		if (properties.getBaseClassMappings() != null
				&& !properties.getBaseClassMappings().isEmpty()) {
			Optional<Map.Entry<String, String>> mapping = properties
					.getBaseClassMappings().entrySet().stream().filter(entry -> {
						String pattern = entry.getKey();
						return contractPathAsPackage.matches(pattern);
					}).findFirst();
			if (log.isDebugEnabled()) {
				log.debug("Matching pattern for contract package ["
						+ contractPathAsPackage + "] with setup "
						+ properties.getBaseClassMappings() + " is [" + mapping + "]");
			}
			if (mapping.isPresent()) {
				return mapping.get().getValue();
			}
		}
		if (StringUtils.isEmpty(properties.getPackageWithBaseClasses())) {
			return properties.getBaseClassForTests();
		}
		String generatedClassName = generateDefaultBaseClassName(contractPackage,
				properties);
		return generatedClassName + "Base";
	}

	private String generateDefaultBaseClassName(String classPackage,
			ContractVerifierConfigProperties properties) {
		String[] splitPackage = NamesUtil.convertIllegalPackageChars(classPackage)
				.split(SEPARATOR);
		if (splitPackage.length > 1) {
			String last = NamesUtil.capitalize(splitPackage[splitPackage.length - 1]);
			String butLast = NamesUtil.capitalize(splitPackage[splitPackage.length - 2]);
			return properties.getPackageWithBaseClasses() + "." + butLast + last;
		}
		return properties.getPackageWithBaseClasses() + "."
				+ NamesUtil.capitalize(splitPackage[0]);
	}

}

class JUnit4OrderClassAnnotation implements ClassAnnotation {

	private final BlockBuilder blockBuilder;

	private final GeneratedClassMetaData generatedClassMetaData;

	private static final String[] ANNOTATIONS = {
			"@FixMethodOrder(MethodSorters.NAME_ASCENDING)" };

	JUnit4OrderClassAnnotation(BlockBuilder blockBuilder,
			GeneratedClassMetaData generatedClassMetaData) {
		this.blockBuilder = blockBuilder;
		this.generatedClassMetaData = generatedClassMetaData;
	}

	@Override
	public ClassAnnotation call() {
		Arrays.stream(ANNOTATIONS).forEach(this.blockBuilder::addIndented);
		return this;
	}

	@Override
	public boolean accept() {
		return this.generatedClassMetaData.configProperties
				.getTestFramework() == TestFramework.JUNIT
				&& this.generatedClassMetaData.listOfFiles.stream()
						.anyMatch(meta -> meta.getOrder() != null);
	}

}

class JUnit4ClassAnnotation implements ClassAnnotation {

	private final BlockBuilder blockBuilder;

	private final GeneratedClassMetaData generatedClassMetaData;

	private static final String[] ANNOTATIONS = { "@Test" };

	JUnit4ClassAnnotation(BlockBuilder blockBuilder,
			GeneratedClassMetaData generatedClassMetaData) {
		this.blockBuilder = blockBuilder;
		this.generatedClassMetaData = generatedClassMetaData;
	}

	@Override
	public ClassAnnotation call() {
		Arrays.stream(ANNOTATIONS).forEach(this.blockBuilder::addIndented);
		return this;
	}

	@Override
	public boolean accept() {
		return this.generatedClassMetaData.configProperties
				.getTestFramework() == TestFramework.JUNIT;
	}

}

class JunitMethodMetadata implements MethodMetadata {

	private final BlockBuilder blockBuilder;

	private final NameProvider nameProvider = new NameProvider();

	JunitMethodMetadata(BlockBuilder blockBuilder) {
		this.blockBuilder = blockBuilder;
	}

	@Override
	public MethodMetadata name(SingleContractMetadata metaData) {
		this.blockBuilder.addAtTheEnd(this.nameProvider.methodName(metaData));
		return this;
	}

	@Override
	public MethodMetadata modifier() {
		this.blockBuilder.addAtTheEnd("public");
		return this;
	}

	@Override
	public MethodMetadata returnType() {
		this.blockBuilder.addAtTheEnd("void");
		return this;
	}

}

class NameProvider {

	private static final Log log = LogFactory.getLog(NameProvider.class);

	String methodName(SingleContractMetadata singleContractMetadata) {
		ContractMetadata contractMetadata = singleContractMetadata.getContractMetadata();
		File stubsFile = contractMetadata.getPath().toFile();
		Contract stubContent = singleContractMetadata.getContract();
		if (StringUtils.hasText(stubContent.getName())) {
			String name = NamesUtil.camelCase(
					NamesUtil.convertIllegalPackageChars(stubContent.getName()));
			if (log.isDebugEnabled()) {
				log.debug("Overriding the default test name with [" + name + "]");
			}
			return name;
		}
		else if (contractMetadata.getConvertedContract().size() > 1) {
			int index = findIndexOf(contractMetadata.getConvertedContract(), stubContent);
			String name = camelCasedMethodFromFileName(stubsFile) + "_" + index;
			if (log.isDebugEnabled()) {
				log.debug(
						"Scenario found. The methodBuilder name will be [" + name + "]");
			}
			return name;
		}
		String name = camelCasedMethodFromFileName(stubsFile);
		if (log.isDebugEnabled()) {
			log.debug("The methodBuilder name will be [" + name + "]");
		}
		return name;
	}

	private int findIndexOf(Collection<Contract> contracts, Contract stubContent) {
		int i = 0;
		for (Contract contract : contracts) {
			if (contract.equals(stubContent)) {
				return i;
			}
			i++;
		}
		return -1;
	}

	private String camelCasedMethodFromFileName(File stubsFile) {
		return NamesUtil.camelCase(NamesUtil.convertIllegalMethodNameChars(NamesUtil
				.toLastDot(NamesUtil.afterLast(stubsFile.getPath(), File.separator))));
	}

}

class RefactoredSingleTestGenerator implements SingleTestGenerator {

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
		// given
		BlockBuilder builder = new BlockBuilder("\t");
		GeneratedClassMetaData metaData = new GeneratedClassMetaData(properties,
				listOfFiles, includedDirectoryRelativePath, generatedClassData);

		SingleMethodBuilder methodBuilder = SingleMethodBuilder.builder(builder)
				.contractMetaData(metaData)
				// JUnitMethodAnnotation
				.methodAnnotation(null)
				// JavaMethodMetadata
				// SpockMethodMetadata
				.methodMetadata(null)
				// MockMvcGiven
				.given(null).given(null)
				// MockMvcWhen
				.when(null).when(null)
				// MockMvcThen
				.then(null).then(null);

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
						new RestAssured3MockMvcImports(builder, metaData))
				.staticImports(new DefaultStaticImports(builder, metaData),
						new CustomStaticImports(builder, metaData),
						new MessagingStaticImports(builder, metaData))
				.classAnnotations(new JUnit4ClassAnnotation(builder, metaData),
						new JUnit4OrderClassAnnotation(builder, metaData))
				.build();

		// SingleTestGenerator requires a String
		return generatedTestClass.asClassString();
	}

	@Override
	public String fileExtension(ContractVerifierConfigProperties properties) {
		return properties.getTestFramework().getClassExtension();
	}

}