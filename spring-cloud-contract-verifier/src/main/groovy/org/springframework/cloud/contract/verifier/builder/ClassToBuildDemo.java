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
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import groovy.json.JsonOutput;
import groovy.lang.Closure;
import groovy.lang.GString;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.text.StringEscapeUtils;

import org.springframework.cloud.contract.spec.Contract;
import org.springframework.cloud.contract.spec.internal.Body;
import org.springframework.cloud.contract.spec.internal.Cookie;
import org.springframework.cloud.contract.spec.internal.DslProperty;
import org.springframework.cloud.contract.spec.internal.ExecutionProperty;
import org.springframework.cloud.contract.spec.internal.FromFileProperty;
import org.springframework.cloud.contract.spec.internal.Header;
import org.springframework.cloud.contract.spec.internal.MatchingStrategy;
import org.springframework.cloud.contract.spec.internal.NamedProperty;
import org.springframework.cloud.contract.spec.internal.Request;
import org.springframework.cloud.contract.verifier.config.ContractVerifierConfigProperties;
import org.springframework.cloud.contract.verifier.config.TestFramework;
import org.springframework.cloud.contract.verifier.config.TestMode;
import org.springframework.cloud.contract.verifier.file.ContractMetadata;
import org.springframework.cloud.contract.verifier.file.SingleContractMetadata;
import org.springframework.cloud.contract.verifier.util.ContentType;
import org.springframework.cloud.contract.verifier.util.MapConverter;
import org.springframework.cloud.contract.verifier.util.NamesUtil;
import org.springframework.util.StringUtils;

import static org.apache.commons.text.StringEscapeUtils.escapeJava;
import static org.springframework.cloud.contract.verifier.util.ContentType.JSON;
import static org.springframework.cloud.contract.verifier.util.ContentUtils.extractValue;
import static org.springframework.cloud.contract.verifier.util.ContentUtils.getJavaMultipartFileParameterContent;
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
			"com.toomuchcoding.jsonassert.JsonAssertion.assertThatJson",
			"org.springframework.cloud.contract.verifier.assertion.SpringCloudContractAssertions.assertThat",
			"org.springframework.cloud.contract.verifier.util.ContractVerifierUtil.*" };

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
				.getTestMode() == TestMode.MOCKMVC
				&& this.generatedClassMetaData.isAnyHttp();
	}

}

class RestAssured3MockMvcStaticImports implements Imports {

	private final BlockBuilder blockBuilder;

	private final GeneratedClassMetaData generatedClassMetaData;

	private static final String[] IMPORTS = {
			"io.restassured.module.mockmvc.RestAssuredMockMvc.*" };

	RestAssured3MockMvcStaticImports(BlockBuilder blockBuilder,
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
				.getTestMode() == TestMode.MOCKMVC;
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

class JUnit4IgnoreMethodAnnotation implements MethodAnnotations {

	private final BlockBuilder blockBuilder;

	private final GeneratedClassMetaData generatedClassMetaData;

	private static final String[] ANNOTATIONS = { "@Ignore" };

	JUnit4IgnoreMethodAnnotation(BlockBuilder blockBuilder,
			GeneratedClassMetaData generatedClassMetaData) {
		this.blockBuilder = blockBuilder;
		this.generatedClassMetaData = generatedClassMetaData;
	}

	@Override
	public boolean accept(SingleContractMetadata singleContractMetadata) {
		return this.generatedClassMetaData.configProperties
				.getTestFramework() == TestFramework.JUNIT
				&& this.generatedClassMetaData.listOfFiles.stream()
						.anyMatch(meta -> meta.getOrder() != null);
	}

	@Override
	public MethodVisitor<MethodAnnotations> apply(
			SingleContractMetadata singleContractMetadata) {
		Arrays.stream(ANNOTATIONS).forEach(this.blockBuilder::addIndented);
		return this;
	}

}

class JUnit4MethodAnnotation implements MethodAnnotations {

	private final BlockBuilder blockBuilder;

	private final GeneratedClassMetaData generatedClassMetaData;

	private static final String[] ANNOTATIONS = { "@Test" };

	JUnit4MethodAnnotation(BlockBuilder blockBuilder,
			GeneratedClassMetaData generatedClassMetaData) {
		this.blockBuilder = blockBuilder;
		this.generatedClassMetaData = generatedClassMetaData;
	}

	@Override
	public boolean accept(SingleContractMetadata singleContractMetadata) {
		return this.generatedClassMetaData.configProperties
				.getTestFramework() == TestFramework.JUNIT;
	}

	@Override
	public MethodVisitor<MethodAnnotations> apply(
			SingleContractMetadata singleContractMetadata) {
		Arrays.stream(ANNOTATIONS).forEach(this.blockBuilder::addIndented);
		return this;
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
		this.blockBuilder.addIndented("public");
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
		if (log.isDebugEnabled()) {
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

class MockMvcGiven implements Given {

	private final BlockBuilder blockBuilder;

	private final GeneratedClassMetaData generatedClassMetaData;

	private final List<Given> givens = new LinkedList<>();

	MockMvcGiven(BlockBuilder blockBuilder,
			GeneratedClassMetaData generatedClassMetaData) {
		this.blockBuilder = blockBuilder;
		this.generatedClassMetaData = generatedClassMetaData;
		this.givens.addAll(Arrays.asList(new MockMvcHeadersGiven(blockBuilder),
				new MockMvcCookiesGiven(blockBuilder),
				new MockMvcBodyGiven(blockBuilder, generatedClassMetaData),
				new MockMvcMultipartGiven(blockBuilder, generatedClassMetaData)));
	}

	@Override
	public MethodVisitor<Given> apply(SingleContractMetadata singleContractMetadata) {
		this.blockBuilder.addIndented("// given:").addEmptyLine().startBlock()
				.addIndented("MockMvcRequestSpecification request = given()");
		List<Given> givens = this.givens.stream()
				.filter(given -> given.accept(singleContractMetadata))
				.collect(Collectors.toList());
		if (givens.isEmpty()) {
			this.blockBuilder.addEnding().addEmptyLine();
			return this;
		}
		this.blockBuilder.addEmptyLine().indent();
		Iterator<Given> iterator = givens.iterator();
		while (iterator.hasNext()) {
			Given given = iterator.next();
			given.apply(singleContractMetadata);
			if (iterator.hasNext()) {
				this.blockBuilder.addEmptyLine();
			}
		}
		this.blockBuilder.addEnding().unindent().endBlock().addEmptyLine();
		return this;
	}

	@Override
	public boolean accept(SingleContractMetadata singleContractMetadata) {
		return this.generatedClassMetaData.configProperties
				.getTestMode() == TestMode.MOCKMVC && singleContractMetadata.isHttp();
	}

}

class MockMvcWhen implements When {

	private final BlockBuilder blockBuilder;

	private final GeneratedClassMetaData generatedClassMetaData;

	MockMvcWhen(BlockBuilder blockBuilder,
			GeneratedClassMetaData generatedClassMetaData) {
		this.blockBuilder = blockBuilder;
		this.generatedClassMetaData = generatedClassMetaData;
	}

	@Override
	public MethodVisitor<When> apply(SingleContractMetadata singleContractMetadata) {
		this.blockBuilder.addIndented("// when:").addEmptyLine().startBlock().endBlock();
		return this;
	}

	@Override
	public boolean accept(SingleContractMetadata singleContractMetadata) {
		return this.generatedClassMetaData.configProperties
				.getTestMode() == TestMode.MOCKMVC && singleContractMetadata.isHttp();
	}

}

class MockMvcHeadersGiven implements Given {

	private final BlockBuilder blockBuilder;

	MockMvcHeadersGiven(BlockBuilder blockBuilder) {
		this.blockBuilder = blockBuilder;
	}

	@Override
	public MethodVisitor<Given> apply(SingleContractMetadata metadata) {
		processInput(this.blockBuilder, metadata.getContract().getRequest());
		return this;
	}

	private void processInput(BlockBuilder bb, Request request) {
		request.getHeaders().executeForEachHeader(header -> {
			if (ofAbsentType(header)) {
				return;
			}
			bb.addIndented(string(header));
		});
	}

	private String string(Header header) {
		return ".header(" + ContentHelper.getTestSideForNonBodyValue(header.getName())
				+ ", " + ContentHelper.getTestSideForNonBodyValue(header.getServerValue())
				+ ")";
	}

	private boolean ofAbsentType(Header header) {
		return header.getServerValue() instanceof MatchingStrategy
				&& MatchingStrategy.Type.ABSENT
						.equals(((MatchingStrategy) header.getServerValue()).getType());
	}

	@Override
	public boolean accept(SingleContractMetadata metadata) {
		Request request = metadata.getContract().getRequest();
		return request != null && request.getHeaders() != null;
	}

}

class MockMvcCookiesGiven implements Given {

	private final BlockBuilder blockBuilder;

	MockMvcCookiesGiven(BlockBuilder blockBuilder) {
		this.blockBuilder = blockBuilder;
	}

	@Override
	public MethodVisitor<Given> apply(SingleContractMetadata metadata) {
		processInput(this.blockBuilder, metadata.getContract().getRequest());
		return this;
	}

	private void processInput(BlockBuilder bb, Request request) {
		request.getCookies().executeForEachCookie(cookie -> {
			if (ofAbsentType(cookie)) {
				return;
			}
			bb.addIndented(string(cookie));
		});
	}

	private String string(Cookie cookie) {
		return ".cookie(" + ContentHelper.getTestSideForNonBodyValue(cookie.getKey())
				+ ", " + ContentHelper.getTestSideForNonBodyValue(cookie.getServerValue())
				+ ")";
	}

	private boolean ofAbsentType(Cookie cookie) {
		return cookie.getServerValue() instanceof MatchingStrategy
				&& MatchingStrategy.Type.ABSENT
						.equals(((MatchingStrategy) cookie.getServerValue()).getType());
	}

	@Override
	public boolean accept(SingleContractMetadata metadata) {
		Request request = metadata.getContract().getRequest();
		return request != null && request.getCookies() != null;
	}

}

class MockMvcBodyGiven implements Given {

	private final BlockBuilder blockBuilder;

	private final BodyReader bodyReader;

	MockMvcBodyGiven(BlockBuilder blockBuilder,
			GeneratedClassMetaData generatedClassMetaData) {
		this.blockBuilder = blockBuilder;
		this.bodyReader = new BodyReader(generatedClassMetaData);
	}

	@Override
	public MethodVisitor<Given> apply(SingleContractMetadata metadata) {
		processInput(this.blockBuilder, metadata);
		return this;
	}

	private void processInput(BlockBuilder bb, SingleContractMetadata metadata) {
		Object body;
		Request request = metadata.getContract().getRequest();
		Object serverValue = request.getBody().getServerValue();
		if (serverValue instanceof ExecutionProperty
				|| serverValue instanceof FromFileProperty) {
			body = request.getBody().getServerValue();
		}
		else {
			body = getBodyAsString(metadata);
		}
		bb.addIndented(getBodyString(metadata, body));
	}

	private String getBodyString(SingleContractMetadata metadata, Object body) {
		String value;
		if (body instanceof ExecutionProperty) {
			value = body.toString();
		}
		else if (body instanceof FromFileProperty) {
			FromFileProperty fileProperty = (FromFileProperty) body;
			value = fileProperty.isByte()
					? this.bodyReader.readBytesFromFileString(metadata, fileProperty,
							CommunicationType.REQUEST)
					: this.bodyReader.readStringFromFileString(metadata, fileProperty,
							CommunicationType.REQUEST);
		}
		else {
			String escaped = escapeRequestSpecialChars(metadata, body.toString());
			value = "\"" + escaped + "\"";
		}
		return ".body(" + value + ")";
	}

	@SuppressWarnings("unchecked")
	private String getBodyAsString(SingleContractMetadata metadata) {
		ContentType contentType = metadata.getInputContentType();
		Body body = metadata.getContract().getRequest().getBody();
		Object bodyValue = extractServerValueFromBody(contentType, body.getServerValue());
		if (contentType == ContentType.FORM) {
			if (bodyValue instanceof Map) {
				// [a:3, b:4] == "a=3&b=4"
				return ((Map) bodyValue).entrySet().stream().map(o -> {
					Map.Entry entry = (Map.Entry) o;
					return convertUnicodeEscapesIfRequired(
							entry.getKey().toString() + "=" + entry.getValue());
				}).collect(Collectors.joining("&")).toString();
			}
			else if (bodyValue instanceof List) {
				// ["a=3", "b=4"] == "a=3&b=4"
				return ((List) bodyValue).stream()
						.map(o -> convertUnicodeEscapesIfRequired(o.toString()))
						.collect(Collectors.joining("&")).toString();
			}
		}
		else {
			String json = JsonOutput.toJson(bodyValue);
			json = convertUnicodeEscapesIfRequired(json);
			return trimRepeatedQuotes(json);
		}
		return "";
	}

	private String convertUnicodeEscapesIfRequired(String json) {
		String unescapedJson = StringEscapeUtils.unescapeEcmaScript(json);
		return escapeJava(unescapedJson);
	}

	private String trimRepeatedQuotes(String toTrim) {
		if (toTrim.startsWith("\"")) {
			return toTrim.replaceAll("\"", "");
			// #261
		}
		else if (toTrim.startsWith("\\\"") && toTrim.endsWith("\\\"")) {
			return toTrim.substring(2, toTrim.length() - 2);
		}
		return toTrim;
	}

	/**
	 * Converts the passed body into ints server side representation. All
	 * {@link DslProperty} will return their server side values
	 */
	private Object extractServerValueFromBody(ContentType contentType, Object bodyValue) {
		if (bodyValue instanceof GString) {
			return extractValue((GString) bodyValue, contentType,
					MapConverterUtils.GET_SERVER_VALUE);
		}
		boolean dontParseStrings = contentType == JSON && bodyValue instanceof Map;
		Closure parsingClosure = dontParseStrings ? Closure.IDENTITY
				: MapConverter.JSON_PARSING_CLOSURE;
		return MapConverter.transformValues(bodyValue, MapConverterUtils.GET_SERVER_VALUE,
				parsingClosure);
	}

	private String escapeRequestSpecialChars(SingleContractMetadata metadata,
			String string) {
		if (metadata.getInputContentType() == ContentType.JSON) {
			return string.replaceAll("\\\\n", "\\\\\\\\n");
		}
		return string;
	}

	@Override
	public boolean accept(SingleContractMetadata metadata) {
		Request request = metadata.getContract().getRequest();
		return request != null && request.getBody() != null;
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
		File newFile = new File(classDataForMethod.testClassPath().getParent().toFile(),
				newFileName);
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
		java.nio.file.Path relativePath = this.generatedClassMetaData.configProperties
				.getGeneratedTestSourcesDir().toPath().relativize(newFile.toPath());
		File newFileInGeneratedTestSources = new File(
				this.generatedClassMetaData.configProperties
						.getGeneratedTestResourcesDir(),
				relativePath.toString());
		newFileInGeneratedTestSources.getParentFile().mkdirs();
		Files.write(newFileInGeneratedTestSources.toPath(), property.asBytes());
	}

}

class MockMvcMultipartGiven implements Given {

	private final BlockBuilder blockBuilder;

	private final BodyReader bodyReader;

	MockMvcMultipartGiven(BlockBuilder blockBuilder,
			GeneratedClassMetaData generatedClassMetaData) {
		this.blockBuilder = blockBuilder;
		this.bodyReader = new BodyReader(generatedClassMetaData);
	}

	@Override
	public MethodVisitor<Given> apply(SingleContractMetadata metadata) {
		processInput(this.blockBuilder, metadata);
		return this;
	}

	private void processInput(BlockBuilder bb, SingleContractMetadata metadata) {
		getMultipartParameters(metadata).entrySet()
				.forEach(entry -> bb.addLine(getMultipartParameterLine(metadata, entry)));
	}

	/**
	 * @return a line of code to send a multi part parameter in the request
	 */
	private String getMultipartParameterLine(SingleContractMetadata metadata,
			Map.Entry<String, Object> parameter) {
		if (parameter.getValue() instanceof NamedProperty) {
			return ".multiPart(" + getMultipartFileParameterContent(metadata,
					parameter.getKey(), (NamedProperty) parameter.getValue()) + ")";
		}
		return getParameterString(parameter);
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> getMultipartParameters(SingleContractMetadata metadata) {
		return (Map<String, Object>) metadata.getContract().getRequest().getMultipart()
				.getServerValue();
	}

	private String getMultipartFileParameterContent(SingleContractMetadata metadata,
			String propertyName, NamedProperty propertyValue) {
		return getJavaMultipartFileParameterContent(propertyName, propertyValue,
				fileProp -> this.bodyReader.readBytesFromFileString(metadata, fileProp,
						CommunicationType.REQUEST));
	}

	private String getParameterString(Map.Entry<String, Object> parameter) {
		return ".param(\"" + escapeJava(parameter.getKey()) + "\", \""
				+ escapeJava((String) parameter.getValue()) + "\")";
	}

	@Override
	public boolean accept(SingleContractMetadata metadata) {
		Request request = metadata.getContract().getRequest();
		return request != null && request.getMultipart() != null;
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
				// MockMvcGiven
				.given(new MockMvcGiven(builder, metaData))
				// // MockMvcWhen
				.when(new MockMvcWhen(builder, metaData))
		// // MockMvcThen
		// .then(null).then(null);
		;

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
						new RestAssured3MockMvcStaticImports(builder, metaData),
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