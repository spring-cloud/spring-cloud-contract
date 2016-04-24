package com.ofg.twitter.places

import com.jayway.restassured.module.mockmvc.RestAssuredMockMvc
import com.ofg.twitter.place.PairIdController
import spock.lang.Specification

// tag::base_class[]
abstract class BaseMockMvcSpec extends Specification {

	def setup() {
		RestAssuredMockMvc.standaloneSetup(new PairIdController())
	}

	void isProperCorrelationId(Integer correlationId) {
		assert correlationId == 123456
	}

	void isEmpty(String value) {
		assert value == null
	}

}
// end::base_class[]
