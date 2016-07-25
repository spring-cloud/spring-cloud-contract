package com.example.fraud;

import com.example.fraud.FraudDetectionController;
import com.jayway.restassured.module.mockmvc.RestAssuredMockMvc;

import org.junit.Before;

public class MvcTest {

	@Before
	public void setup() {
		RestAssuredMockMvc.standaloneSetup(new FraudDetectionController());
	}

	public void assertThatRejectionReasonIsNull(Object rejectionReason) {
		assert rejectionReason == null;
	}
}