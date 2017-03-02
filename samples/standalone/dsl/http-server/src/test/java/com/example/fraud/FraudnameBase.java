package com.example.fraud;

import org.junit.Before;

import com.jayway.restassured.module.mockmvc.RestAssuredMockMvc;

public class FraudnameBase {

	private static final String FRAUD_NAME = "fraud";

	FraudVerifier fraudVerifier = FRAUD_NAME::equals;

	@Before
	public void setup() {
		RestAssuredMockMvc.standaloneSetup(new FraudNameController(this.fraudVerifier));
	}
}