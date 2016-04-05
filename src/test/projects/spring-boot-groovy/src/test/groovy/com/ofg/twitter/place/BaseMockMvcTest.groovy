package com.ofg.twitter.place

import com.jayway.restassured.module.mockmvc.RestAssuredMockMvc
import com.ofg.twitter.place.PairIdController
import spock.lang.Specification

abstract class BaseMockMvcTest extends Specification {

	def setup() {
		RestAssuredMockMvc.standaloneSetup(new PairIdController())
	}

	void isProperCorrelationId(Integer correlationId) {
		assert correlationId == 123456
	}
}
