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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.text.StringEscapeUtils;

import org.springframework.cloud.contract.spec.Contract;
import org.springframework.cloud.contract.spec.internal.ExecutionProperty;
import org.springframework.cloud.contract.spec.internal.FromFileProperty;
import org.springframework.cloud.contract.verifier.config.ContractVerifierConfigProperties;
import org.springframework.cloud.contract.verifier.config.TestFramework;
import org.springframework.cloud.contract.verifier.file.ContractMetadata;
import org.springframework.cloud.contract.verifier.file.SingleContractMetadata;
import org.springframework.cloud.contract.verifier.util.MapConverter;
import org.springframework.cloud.contract.verifier.util.NamesUtil;
import org.springframework.util.Assert;
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
		return this.generatedClassMetaData.configProperties.getImports() != null
				&& this.generatedClassMetaData.configProperties.getImports().length > 0;
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
		return this.generatedClassMetaData.configProperties.getStaticImports() != null
				&& this.generatedClassMetaData.configProperties
						.getStaticImports().length > 0;
	}

}

class DefaultImports implements Imports, DefaultBaseClassProvider {

	private final BlockBuilder blockBuilder;

	private final GeneratedClassMetaData generatedClassMetaData;

	private final BaseClassProvider baseClassProvider;

	DefaultImports(BlockBuilder blockBuilder,
			GeneratedClassMetaData generatedClassMetaData) {
		this.blockBuilder = blockBuilder;
		this.generatedClassMetaData = generatedClassMetaData;
		this.baseClassProvider = new BaseClassProvider();
	}

	@Override
	public Imports call() {
		String fqnBaseClass = fqnBaseClass();
		if (StringUtils.hasText(fqnBaseClass)) {
			this.blockBuilder.addLineWithEnding("import " + fqnBaseClass);
		}
		return this;
	}

	@Override
	public boolean accept() {
		return true;
	}

	@Override
	public GeneratedClassMetaData generatedClassMetaData() {
		return this.generatedClassMetaData;
	}

	@Override
	public BaseClassProvider baseClassProvider() {
		return this.baseClassProvider;
	}

}

class DefaultStaticImports implements Imports {

	private final BlockBuilder blockBuilder;

	private static final String[] IMPORTS = {
			"org.springframework.cloud.contract.verifier.assertion.SpringCloudContractAssertions.assertThat",
			"org.springframework.cloud.contract.verifier.util.ContractVerifierUtil.*" };

	DefaultStaticImports(BlockBuilder blockBuilder) {
		this.blockBuilder = blockBuilder;
	}

	@Override
	public Imports call() {
		Arrays.stream(IMPORTS)
				.forEach(s -> this.blockBuilder.addLineWithEnding("import static " + s));
		return this;
	}

	@Override
	public boolean accept() {
		return true;
	}

}

class DefaultJsonStaticImports implements Imports {

	private final BlockBuilder blockBuilder;

	private final GeneratedClassMetaData generatedClassMetaData;

	private static final String[] IMPORTS = {
			"com.toomuchcoding.jsonassert.JsonAssertion.assertThatJson" };

	DefaultJsonStaticImports(BlockBuilder blockBuilder,
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
						.anyMatch(SingleContractMetadata::isJson));
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

interface DefaultBaseClassProvider {

	GeneratedClassMetaData generatedClassMetaData();

	BaseClassProvider baseClassProvider();

	default String fqnBaseClass() {
		ContractVerifierConfigProperties properties = generatedClassMetaData().configProperties;
		String includedDirectoryRelativePath = generatedClassMetaData().includedDirectoryRelativePath;
		return baseClassProvider().retrieveBaseClass(properties,
				includedDirectoryRelativePath);
	}

}

interface DefaultClassMetadata extends ClassMetaData, DefaultBaseClassProvider {

	@Override
	default ClassMetaData packageDefinition() {
		blockBuilder().addLineWithEnding(
				"package " + generatedClassMetaData().generatedClassData.classPackage);
		return this;
	}

	GeneratedClassMetaData generatedClassMetaData();

	BaseClassProvider baseClassProvider();

	BlockBuilder blockBuilder();

	@Override
	default ClassMetaData className() {
		String className = capitalize(
				generatedClassMetaData().generatedClassData.className);
		blockBuilder().addAtTheEnd(className);
		return this;
	}

}

class JavaClassMetaData implements ClassMetaData, DefaultClassMetadata {

	private final BlockBuilder blockBuilder;

	private final BaseClassProvider baseClassProvider = new BaseClassProvider();

	private final GeneratedClassMetaData generatedClassMetaData;

	JavaClassMetaData(BlockBuilder blockBuilder,
			GeneratedClassMetaData generatedClassMetaData) {
		this.blockBuilder = blockBuilder;
		this.generatedClassMetaData = generatedClassMetaData;
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
						: "Test";
		if (!this.blockBuilder.endsWith(suffix)) {
			this.blockBuilder.addAtTheEnd(suffix);
		}
		return this;
	}

	@Override
	public ClassMetaData setupLineEnding() {
		this.blockBuilder.setupLineEnding(";");
		return this;
	}

	@Override
	public ClassMetaData setupLabelPrefix() {
		this.blockBuilder.setupLabelPrefix("// ");
		return this;
	}

	@Override
	public GeneratedClassMetaData generatedClassMetaData() {
		return this.generatedClassMetaData;
	}

	@Override
	public BaseClassProvider baseClassProvider() {
		return this.baseClassProvider;
	}

	@Override
	public BlockBuilder blockBuilder() {
		return this.blockBuilder;
	}

	@Override
	public ClassMetaData parentClass() {
		String baseClass = fqnBaseClass();
		if (StringUtils.hasText(baseClass)) {
			int lastIndexOf = baseClass.lastIndexOf(".");
			if (lastIndexOf > 0) {
				baseClass = baseClass.substring(lastIndexOf + 1);
			}
			blockBuilder().append("extends ").append(baseClass).append(" ");
		}
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

class GroovyClassMetaData implements ClassMetaData, DefaultClassMetadata {

	private final BlockBuilder blockBuilder;

	private final BaseClassProvider baseClassProvider = new BaseClassProvider();

	private final GeneratedClassMetaData generatedClassMetaData;

	GroovyClassMetaData(BlockBuilder blockBuilder,
			GeneratedClassMetaData generatedClassMetaData) {
		this.blockBuilder = blockBuilder;
		this.generatedClassMetaData = generatedClassMetaData;
	}

	@Override
	public ClassMetaData setupLineEnding() {
		return this;
	}

	@Override
	public ClassMetaData setupLabelPrefix() {
		return this;
	}

	@Override
	public ClassMetaData suffix() {
		String suffix = StringUtils.hasText(
				this.generatedClassMetaData.configProperties.getNameSuffixForTests())
						? this.generatedClassMetaData.configProperties
								.getNameSuffixForTests()
						: "Spec";
		if (!this.blockBuilder.endsWith(suffix)) {
			this.blockBuilder.addAtTheEnd(suffix);
		}
		return this;
	}

	@Override
	public ClassMetaData modifier() {
		return this;
	}

	@Override
	public ClassMetaData packageDefinition() {
		this.blockBuilder.addLineWithEnding(
				"package " + this.generatedClassMetaData.generatedClassData.classPackage);
		return this;
	}

	@Override
	public ClassMetaData parentClass() {
		ContractVerifierConfigProperties properties = generatedClassMetaData().configProperties;
		String includedDirectoryRelativePath = generatedClassMetaData().includedDirectoryRelativePath;
		String baseClass = baseClassProvider().retrieveBaseClass(properties,
				includedDirectoryRelativePath);
		baseClass = StringUtils.hasText(baseClass) ? baseClass : "Specification";
		int lastIndexOf = baseClass.lastIndexOf(".");
		if (lastIndexOf > 0) {
			baseClass = baseClass.substring(lastIndexOf + 1);
		}
		blockBuilder().append("extends ").append(baseClass).append(" ");
		return this;
	}

	@Override
	public GeneratedClassMetaData generatedClassMetaData() {
		return this.generatedClassMetaData;
	}

	@Override
	public BaseClassProvider baseClassProvider() {
		return this.baseClassProvider;
	}

	@Override
	public BlockBuilder blockBuilder() {
		return this.blockBuilder;
	}

	@Override
	public boolean accept() {
		return this.generatedClassMetaData.configProperties
				.getTestFramework() == TestFramework.SPOCK;
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

class JUnitMethodMetadata implements MethodMetadata {

	private final BlockBuilder blockBuilder;

	private final GeneratedClassMetaData metaData;

	private final NameProvider nameProvider = new NameProvider();

	JUnitMethodMetadata(BlockBuilder blockBuilder, GeneratedClassMetaData metaData) {
		this.blockBuilder = blockBuilder;
		this.metaData = metaData;
	}

	@Override
	public MethodMetadata name(SingleContractMetadata metaData) {
		this.blockBuilder.addAtTheEnd(this.nameProvider.methodName(metaData));
		return this;
	}

	@Override
	public MethodMetadata modifier() {
		this.blockBuilder.addIndented("public");
		return this;
	}

	@Override
	public MethodMetadata returnType() {
		this.blockBuilder.append("void");
		return this;
	}

	@Override
	public boolean accept() {
		return this.metaData.configProperties.getTestFramework() != TestFramework.SPOCK;
	}

}

class SpockMethodMetadata implements MethodMetadata {

	private final BlockBuilder blockBuilder;

	private final GeneratedClassMetaData metaData;

	private final NameProvider nameProvider = new NameProvider();

	SpockMethodMetadata(BlockBuilder blockBuilder, GeneratedClassMetaData metaData) {
		this.blockBuilder = blockBuilder;
		this.metaData = metaData;
	}

	@Override
	public MethodMetadata name(SingleContractMetadata metaData) {
		this.blockBuilder.addAtTheEnd(this.nameProvider.methodName(metaData));
		return this;
	}

	@Override
	public MethodMetadata modifier() {
		return this;
	}

	@Override
	public MethodMetadata returnType() {
		this.blockBuilder.addIndented("def");
		return this;
	}

	@Override
	public boolean accept() {
		return this.metaData.configProperties.getTestFramework() == TestFramework.SPOCK;
	}

}

class NameProvider {

	private static final Log log = LogFactory.getLog(NameProvider.class);

	String methodName(SingleContractMetadata singleContractMetadata) {
		return "validate_" + generateMethodName(singleContractMetadata);
	}

	private String generateMethodName(SingleContractMetadata singleContractMetadata) {
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
				log.debug("Scenario found. The method name will be [" + name + "]");
			}
			return name;
		}
		String name = camelCasedMethodFromFileName(stubsFile);
		if (StringUtils.hasText(name) && log.isDebugEnabled()) {
			log.debug("The method name will be [" + name + "]");
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
		return "new String(" + readBytesFromFileString(metadata, property, side) + ")";
	}

	private String byteBodyToAFileForTestMethod(SingleContractMetadata metadata,
			FromFileProperty property, CommunicationType side) {
		GeneratedClassDataForMethod classDataForMethod = new GeneratedClassDataForMethod(
				this.generatedClassMetaData.generatedClassData, metadata.methodName());
		String newFileName = classDataForMethod.getMethodName() + "_"
				+ side.name().toLowerCase() + "_" + property.fileName();
		Path parent = classDataForMethod.testClassPath().getParent();
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
		java.nio.file.Path relativePath = path.relativize(newFile.toPath());
		File newFileInGeneratedTestSources = new File(
				this.generatedClassMetaData.configProperties
						.getGeneratedTestResourcesDir(),
				relativePath.toString());
		newFileInGeneratedTestSources.getParentFile().mkdirs();
		Files.write(newFileInGeneratedTestSources.toPath(), property.asBytes());
	}

}

/**
 * Adds a label, proper indents and line endings for the body of a method.
 */
interface BodyMethodVisitor {

	/**
	 * Adds a starting body method block. E.g. //given: together with all indents
	 * @param blockBuilder
	 * @param label
	 * @return
	 */
	default BlockBuilder startBodyBlock(BlockBuilder blockBuilder, String label) {
		return blockBuilder.addIndentation().appendWithLabelPrefix(label).addEmptyLine()
				.startBlock();
	}

	/**
	 * Picks matching elements, visits them and applies indents.
	 * @param blockBuilder
	 * @param methodVisitors
	 * @param singleContractMetadata
	 */
	default void indentedBodyBlock(BlockBuilder blockBuilder,
			List<? extends MethodVisitor> methodVisitors,
			SingleContractMetadata singleContractMetadata) {
		List<MethodVisitor> visitors = filterVisitors(methodVisitors,
				singleContractMetadata);
		if (visitors.isEmpty()) {
			blockBuilder.addEndingIfNotPresent().addEmptyLine();
			blockBuilder.endBlock();
			return;
		}
		blockBuilder.addEmptyLine().indent();
		applyVisitors(blockBuilder, singleContractMetadata, visitors);
		endIndentedBodyBlock(blockBuilder);
	}

	/**
	 * Picks matching visitors.
	 * @param methodVisitors
	 * @param singleContractMetadata
	 * @return
	 */
	default List<MethodVisitor> filterVisitors(
			List<? extends MethodVisitor> methodVisitors,
			SingleContractMetadata singleContractMetadata) {
		return methodVisitors.stream()
				.filter(given -> given.accept(singleContractMetadata))
				.collect(Collectors.toList());
	}

	/**
	 * Picks matching elements, visits them. Doesn't apply indents. Useful for the //
	 * then: block where there is no method chaining.
	 * @param blockBuilder
	 * @param methodVisitors
	 * @param singleContractMetadata
	 */
	default void bodyBlock(BlockBuilder blockBuilder,
			List<? extends MethodVisitor> methodVisitors,
			SingleContractMetadata singleContractMetadata) {
		List<MethodVisitor> visitors = filterVisitors(methodVisitors,
				singleContractMetadata);
		if (visitors.isEmpty()) {
			blockBuilder.addEndingIfNotPresent().addEmptyLine();
			return;
		}
		applyVisitorsWithEnding(blockBuilder, singleContractMetadata, visitors);
		endBodyBlock(blockBuilder);
	}

	/**
	 * Executes logic for all the matching visitors.
	 * @param blockBuilder
	 * @param singleContractMetadata
	 * @param visitors
	 */
	default void applyVisitors(BlockBuilder blockBuilder,
			SingleContractMetadata singleContractMetadata, List<MethodVisitor> visitors) {
		Iterator<MethodVisitor> iterator = visitors.iterator();
		while (iterator.hasNext()) {
			MethodVisitor visitor = iterator.next();
			visitor.apply(singleContractMetadata);
			if (iterator.hasNext()) {
				blockBuilder.addEmptyLine();
			}
		}
		blockBuilder.addEndingIfNotPresent();
	}

	/**
	 * Executes logic for all the matching visitors.
	 * @param blockBuilder
	 * @param singleContractMetadata
	 * @param visitors
	 */
	default void applyVisitorsWithEnding(BlockBuilder blockBuilder,
			SingleContractMetadata singleContractMetadata, List<MethodVisitor> visitors) {
		Iterator<MethodVisitor> iterator = visitors.iterator();
		while (iterator.hasNext()) {
			MethodVisitor visitor = iterator.next();
			visitor.apply(singleContractMetadata);
			blockBuilder.addEndingIfNotPresent();
			if (iterator.hasNext()) {
				blockBuilder.addEmptyLine();
			}
		}
	}

	default void endIndentedBodyBlock(BlockBuilder blockBuilder) {
		blockBuilder.addEndingIfNotPresent().unindent().endBlock();
	}

	default void endBodyBlock(BlockBuilder blockBuilder) {
		blockBuilder.addEndingIfNotPresent().endBlock();
	}

}

final class ContentHelper {

	/**
	 * Depending on the object type extracts the test side values and combines them into a
	 * String representation. Unlike the body transformation done via
	 * {@link MethodBodyBuilder#getTestSideValue(java.lang.Object)} will not try to guess
	 * the type of the value of the header (e.g. if it's a JSON).
	 */
	static String getTestSideForNonBodyValue(Object object) {
		if (object instanceof ExecutionProperty) {
			return getTestSideValue(object);
		}
		return quotedAndEscaped(
				MapConverter.getTestSideValuesForNonBody(object).toString());
	}

	/**
	 * Depending on the object type extracts the test side values and combines them into a
	 * String representation
	 */
	private static String getTestSideValue(Object object) {
		if (object instanceof ExecutionProperty) {
			return object.toString();
		}
		return '"' + MapConverter.getTestSideValues(object).toString() + '"';
	}

	private static String quotedAndEscaped(String string) {
		return '"' + StringEscapeUtils.escapeJava(string) + '"';
	}

}