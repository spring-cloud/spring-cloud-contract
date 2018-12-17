package com.example.fraud;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.Before;

public class FraudnameBase {

	private static final String FRAUD_NAME = "fraud";

	FraudVerifier fraudVerifier = FRAUD_NAME::equals;

	@Before
	public void setup() {
		RestAssuredMockMvc.standaloneSetup(new FraudNameController(this.fraudVerifier));
	}
}