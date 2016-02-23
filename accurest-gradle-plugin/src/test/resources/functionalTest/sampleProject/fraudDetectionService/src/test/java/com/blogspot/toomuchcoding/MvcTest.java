package com.blogspot.toomuchcoding;

import com.blogspot.toomuchcoding.frauddetection.FraudDetectionController;
import com.jayway.restassured.module.mockmvc.RestAssuredMockMvc;
import spock.lang.Specification;
import org.junit.Before;

public class MvcTest {

	@Before
	public void setup() {
		RestAssuredMockMvc.standaloneSetup(new com.blogspot.toomuchcoding.frauddetection.FraudDetectionController());
	}

	public void assertThatRejectionReasonIsNull(Object rejectionReason) {
		assert rejectionReason == null;
	}
}