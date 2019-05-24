package org.springframework.cloud.contract.verifier.builder;

import org.junit.Test;

public class GeneratedTestClassTests {

	@Test
	public void should_work_for_junit4_mockmvc_json_non_binary() {
		// given
		BlockBuilder builder = new BlockBuilder(" ");
		GeneratedClassMetaData metaData = null;

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
				.classAnnotations(new JUnit4OrderClassAnnotation(builder, metaData))
				.build();

		// SingleTestGenerator requires a String
		String contentsOfASingleClass = generatedTestClass.asClassString();
	}

}