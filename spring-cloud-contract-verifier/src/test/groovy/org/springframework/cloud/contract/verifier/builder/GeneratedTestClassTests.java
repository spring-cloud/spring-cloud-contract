package org.springframework.cloud.contract.verifier.builder;

import java.io.File;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;

import org.assertj.core.api.BDDAssertions;
import org.junit.Test;

import org.springframework.cloud.contract.verifier.config.ContractVerifierConfigProperties;
import org.springframework.cloud.contract.verifier.file.ContractMetadata;

public class GeneratedTestClassTests {

	@Test
	public void should_work_for_junit4_mockmvc_json_non_binary() {
		//given
		RefactoredSingleTestGenerator generator = new RefactoredSingleTestGenerator();
		ContractVerifierConfigProperties configProperties = new ContractVerifierConfigProperties();
		Collection<ContractMetadata> contracts = Collections.emptyList();
		String includedDirectoryRelativePath = "some/path";
		String convertedClassName = "fooBar";
		String packageName = "com.example";
		Path classPath = new File("/tmp").toPath();
		configProperties.setBaseClassForTests("BazBar");

		//when
		String builtClass = generator.buildClass(configProperties, contracts, includedDirectoryRelativePath,
				new SingleTestGenerator.GeneratedClassData(convertedClassName, packageName, classPath));

		//then
		BDDAssertions.then(builtClass).isNotEmpty();
	}

}