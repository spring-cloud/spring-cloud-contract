// tag::base_class[]
package com.example.fraud;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.springframework.restdocs.JUnitRestDocumentation;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;

public abstract class FraudBaseWithStandaloneSetup {

	private static final String OUTPUT = "target/generated-snippets";

	@Rule
	public JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation(OUTPUT);

	@Rule public TestName testName = new TestName();

	@Before
	public void setup() {
		RestAssuredMockMvc.standaloneSetup(MockMvcBuilders.standaloneSetup(new FraudDetectionController())
				.apply(documentationConfiguration(this.restDocumentation))
				.alwaysDo(document(getClass().getSimpleName() + "_" + testName.getMethodName())));
	}

}
// end::base_class[]