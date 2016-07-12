package com.toomuchcoding

import org.springframework.cloud.contract.frauddetection.FraudDetectionController
import com.jayway.restassured.module.mockmvc.RestAssuredMockMvc
import spock.lang.Specification

class MvcSpec extends Specification {
	def setup() {
		RestAssuredMockMvc.standaloneSetup(new FraudDetectionController())
	}

	void assertThatRejectionReasonIsNull(def rejectionReason) {
		assert !rejectionReason
	}
}
