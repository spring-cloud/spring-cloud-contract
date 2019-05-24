package org.springframework.cloud.contract.verifier.builder;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.cloud.contract.verifier.config.ContractVerifierConfigProperties;
import org.springframework.cloud.contract.verifier.config.TestFramework;
import org.springframework.cloud.contract.verifier.util.NamesUtil;
import org.springframework.util.StringUtils;

import static org.springframework.cloud.contract.verifier.util.NamesUtil.capitalize;

class ClassToBuildDemo {

}

class CustomImports implements Imports {

	private final BlockBuilder blockBuilder;

	private final ContractMetaData contractMetaData;

	CustomImports(BlockBuilder blockBuilder, List<ContractMetaData> contractMetaData) {
		this.blockBuilder = blockBuilder;
		this.contractMetaData = contractMetaData.get(0);
	}

	@Override
	public Imports call() {
		Arrays.stream(this.contractMetaData.configProperties.getImports())
				.forEach(s -> this.blockBuilder.addLineWithEnding("import " + s));
		return this;
	}

	@Override
	public boolean accept() {
		return this.contractMetaData.configProperties.getImports().length > 0;
	}

}

class JUnit4Imports implements Imports {

	private final BlockBuilder blockBuilder;

	private final ContractMetaData contractMetaData;

	private static final String[] IMPORTS = { "org.junit.Test", "org.junit.Rule" };

	JUnit4Imports(BlockBuilder blockBuilder, List<ContractMetaData> contractMetaData) {
		this.blockBuilder = blockBuilder;
		this.contractMetaData = contractMetaData.get(0);
	}

	@Override
	public Imports call() {
		Arrays.stream(IMPORTS)
				.forEach(s -> this.blockBuilder.addLineWithEnding("import " + s));
		return this;
	}

	@Override
	public boolean accept() {
		return this.contractMetaData.configProperties
				.getTestFramework() == TestFramework.JUNIT;
	}

}

class JsonImports implements Imports {

	private final BlockBuilder blockBuilder;

	JsonImports(BlockBuilder blockBuilder) {
		this.blockBuilder = blockBuilder;
	}

	@Override
	public Imports call() {
		return null;
	}

	@Override
	public boolean accept() {
		return false;
	}

}

class MockMvcImports implements Imports {

	private final BlockBuilder blockBuilder;

	MockMvcImports(BlockBuilder blockBuilder) {
		this.blockBuilder = blockBuilder;
	}

	@Override
	public Imports call() {
		return null;
	}

	@Override
	public boolean accept() {
		return false;
	}

}

class JavaClassMetaData implements ClassMetaData {

	private final BlockBuilder blockBuilder;

	private final BaseClassRetriever baseClassRetriever = new BaseClassRetriever();

	private final ContractMetaData contractMetaData;

	JavaClassMetaData(BlockBuilder blockBuilder,
			List<ContractMetaData> contractMetaData) {
		this.blockBuilder = blockBuilder;
		this.contractMetaData = contractMetaData.get(0);
	}

	@Override
	public ClassMetaData packageDefinition() {
		this.blockBuilder.addLineWithEnding(
				"package " + this.contractMetaData.generatedClassData.classPackage);
		return this;
	}

	@Override
	public ClassMetaData modifier() {
		this.blockBuilder.addAtTheEnd("public");
		return this;
	}

	@Override
	public ClassMetaData suffix() {
		String suffix = StringUtils
				.hasText(this.contractMetaData.configProperties.getNameSuffixForTests())
						? this.contractMetaData.configProperties.getNameSuffixForTests()
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
		ContractVerifierConfigProperties properties = this.contractMetaData.configProperties;
		String includedDirectoryRelativePath = this.contractMetaData.includedDirectoryRelativePath;
		String baseClass = this.baseClassRetriever.retrieveBaseClass(properties,
				includedDirectoryRelativePath);
		this.blockBuilder.addAtTheEnd(baseClass);
		return this;
	}

	@Override
	public ClassMetaData className() {
		String className = capitalize(this.contractMetaData.generatedClassData.className);
		this.blockBuilder.addAtTheEnd(className);
		return this;
	}

	@Override
	public boolean accept() {
		return this.contractMetaData.configProperties
				.getTestFramework() == TestFramework.JUNIT
				|| this.contractMetaData.configProperties
						.getTestFramework() == TestFramework.JUNIT5;
	}

}

class BaseClassRetriever {

	private static final Log log = LogFactory.getLog(BaseClassRetriever.class);

	private static final String SEPARATOR = "_REPLACEME_";

	String retrieveBaseClass(ContractVerifierConfigProperties properties,
			String includedDirectoryRelativePath) {
		String contractPathAsPackage = includedDirectoryRelativePath
				.replace(File.separator, ".");
		String contractPackage = includedDirectoryRelativePath.replace(File.separator,
				SEPARATOR);
		// package mapping takes super precedence
		if (!properties.getBaseClassMappings().isEmpty()) {
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

class JUnit4ClassAnnotation implements ClassAnnotation {

	private final BlockBuilder blockBuilder;

	JUnit4ClassAnnotation(BlockBuilder blockBuilder) {
		this.blockBuilder = blockBuilder;
	}

	@Override
	public boolean accept() {
		return false;
	}

	@Override
	public ClassAnnotation call() {
		return null;
	}

}