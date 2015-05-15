package com.blogspot.toomuchcoding

import com.blogspot.toomuchcoding.frauddetection.FraudDetectionController
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
