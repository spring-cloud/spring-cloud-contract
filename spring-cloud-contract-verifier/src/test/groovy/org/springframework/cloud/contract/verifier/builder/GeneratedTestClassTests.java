package org.springframework.cloud.contract.verifier.builder;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class GeneratedTestClassTests {

	@Test
	public void should_work_for_junit4_mockmvc_json_non_binary() {
		//given
		BlockBuilder blockBuilder = new BlockBuilder(" ");
		List<ContractMetaData> contractMetaData = new ArrayList<>();

		SingleMethodBuilder methodBuilder = SingleMethodBuilder.builder(blockBuilder)
				// JUnitMethodAnnotation
				.methodAnnotation(null)
				// JavaMethodMetadata
				// SpockMethodMetadata
				.methodMetadata(null)
				.contractMetaData(contractMetaData)
				// MockMvcGiven
				.given(null).given(null)
				// MockMvcWhen
				.when(null).when(null)
				// MockMvcThen
				.then(null).then(null);

		ClassBodyBuilder bodyBuilder = ClassBodyBuilder.builder(blockBuilder)
				// Junit5Field
				.field(null)
				.field(null)
				.field(null)
				.methodBuilder(methodBuilder);

		GeneratedTestClass generatedTestClass = GeneratedTestClassBuilder
				.builder(blockBuilder)
				.classBodyBuilder(bodyBuilder)
				// SpockMetaData
				.metaData(null)
				.imports(new JsonImports(blockBuilder), new JUnit4Imports(blockBuilder, contractMetaData))
				.classAnnotations(new JUnit4ClassAnnotation(blockBuilder)).build();

		// SingleTestGenerator requires a String
		String contentsOfASingleClass = generatedTestClass.asClassString();
	}

}