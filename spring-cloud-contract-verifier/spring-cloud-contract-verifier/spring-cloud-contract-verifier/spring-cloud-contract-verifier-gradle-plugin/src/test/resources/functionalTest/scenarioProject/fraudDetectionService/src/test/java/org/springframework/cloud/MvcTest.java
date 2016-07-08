package org.springframework.cloud;

import com.jayway.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.Before;

public class MvcTest {

	@Before
	public void setup() {
		RestAssuredMockMvc.standaloneSetup(new org.springframework.cloud.frauddetection.FraudDetectionController());
	}

	public void assertThatRejectionReasonIsNull(Object rejectionReason) {
		assert rejectionReason == null;
	}
}