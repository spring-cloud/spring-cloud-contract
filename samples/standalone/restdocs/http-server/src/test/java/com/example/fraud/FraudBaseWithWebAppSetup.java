// tag::base_class[]
package com.example.fraud;

import io.restassured.module.mockmvc.RestAssuredMockMvc;

import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.restdocs.JUnitRestDocumentation;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public abstract class FraudBaseWithWebAppSetup {

	private static final String OUTPUT = "target/generated-snippets";

	@Rule
	public JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation(OUTPUT);

	@Rule public TestName testName = new TestName();

	@Autowired
	private WebApplicationContext context;

	@Before
	public void setup() {
	RestAssuredMockMvc.mockMvc(MockMvcBuilders.webAppContextSetup(this.context)
			.apply(documentationConfiguration(this.restDocumentation))
			.alwaysDo(document(getClass().getSimpleName() + "_" + testName.getMethodName()))
			.build());
	}

	protected void assertThatRejectionReasonIsNull(Object rejectionReason) {
		assert rejectionReason == null;
	}
}
// end::base_class[]