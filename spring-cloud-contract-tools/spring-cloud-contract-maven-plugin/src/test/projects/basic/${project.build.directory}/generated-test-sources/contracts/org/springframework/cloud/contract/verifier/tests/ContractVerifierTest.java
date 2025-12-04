package org.springframework.cloud.contract.verifier.tests;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Disabled;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierObjectMapper;
import org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierMessage;
import org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierMessaging;
import io.restassured.module.mockmvc.specification.MockMvcRequestSpecification;
import io.restassured.response.ResponseOptions;

import static org.springframework.cloud.contract.verifier.assertion.SpringCloudContractAssertions.assertThat;
import static org.springframework.cloud.contract.verifier.util.ContractVerifierUtil.*;
import static com.toomuchcoding.jsonassert.JsonAssertion.assertThatJson;
import static org.springframework.cloud.contract.verifier.messaging.util.ContractVerifierMessagingUtil.headers;
import static org.springframework.cloud.contract.verifier.util.ContractVerifierUtil.fileToBytes;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.*;

@SuppressWarnings("rawtypes")
public class ContractVerifierTest {
	@Autowired ContractVerifierMessaging contractVerifierMessaging;
	@Autowired ContractVerifierObjectMapper contractVerifierObjectMapper;

	@Test
	public void validate_messaging() throws Exception {
		// when:
			hashCode();

		// then:
			ContractVerifierMessage response = contractVerifierMessaging.receive("jms:output",
					contract(this, "messaging.yml"));
			assertThat(response).isNotNull();

		// and:
			assertThat(response.getHeader("BOOK-NAME")).isNotNull();
			assertThat(response.getHeader("BOOK-NAME").toString()).isEqualTo("foo");

		// and:
			DocumentContext parsedJson = JsonPath.parse(contractVerifierObjectMapper.writeValueAsString(response.getPayload()));
			assertThatJson(parsedJson).field("['bookName']").isEqualTo("foo");
	}

	@Test
	public void validate_sample() throws Exception {
		// given:
			MockMvcRequestSpecification request = given()
					.header("Content-Type", "application/json")
					.body("{\"login\":\"john\",\"name\":\"John The Contract\"}");

		// when:
			ResponseOptions response = given().spec(request)
					.post("/users");

		// then:
			assertThat(response.statusCode()).isEqualTo(200);
			assertThat(response.header("Location")).isEqualTo("/users/john");
	}

	@Test
	@Disabled
	public void validate_withList() throws Exception {
		// given:
			MockMvcRequestSpecification request = given()
					.header("Content-Type", "application/json");

		// when:
			ResponseOptions response = given().spec(request)
					.post("/users");

		// then:
			assertThat(response.statusCode()).isEqualTo(200);
			assertThat(response.header("Location")).isEqualTo("/users/john");

		// and:
			DocumentContext parsedJson = JsonPath.parse(response.getBody().asString());
			assertThatJson(parsedJson).array("['list']").arrayField().isEqualTo("login").value();
			assertThatJson(parsedJson).array("['list']").arrayField().isEqualTo("john").value();
			assertThatJson(parsedJson).array("['list']").arrayField().isEqualTo("name").value();
			assertThatJson(parsedJson).array("['list']").arrayField().isEqualTo("John The Contract").value();
	}

}
