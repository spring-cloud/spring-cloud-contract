/*
 * Copyright 2013-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.contract.stubrunner.server

import groovy.json.JsonSlurper
import io.restassured.module.mockmvc.RestAssuredMockMvc
import org.assertj.core.api.BDDAssertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.contract.stubrunner.StubRunning
import org.springframework.test.context.ActiveProfiles

/**
 * @author Marcin Grzejszczak
 */
// tag::boot_usage[]
@SpringBootTest(classes = StubRunnerBoot, properties = "spring.cloud.zookeeper.enabled=false")
@ActiveProfiles("test")
class StubRunnerBootSpec {

	@Autowired
	StubRunning stubRunning

	@BeforeEach
	void setup() {
		RestAssuredMockMvc.standaloneSetup(new HttpStubsController(stubRunning),
				new TriggerController(stubRunning))
	}

	@Test
	void 'should return a list of running stub servers in "full ivy port" notation'() {
		when:
			String response = RestAssuredMockMvc.get('/stubs').body.asString()
		then:
			def root = new JsonSlurper().parseText(response)
			assert root.'org.springframework.cloud.contract.verifier.stubs:bootService:0.0.1-SNAPSHOT:stubs' instanceof Integer
	}

	@Test
	void 'should return a port on which a #stubId stub is running'() {
		given:
		def stubIds = ['org.springframework.cloud.contract.verifier.stubs:bootService:+:stubs',
				   'org.springframework.cloud.contract.verifier.stubs:bootService:0.0.1-SNAPSHOT:stubs',
				   'org.springframework.cloud.contract.verifier.stubs:bootService:+',
				   'org.springframework.cloud.contract.verifier.stubs:bootService',
				   'bootService']
		stubIds.each {
			when:
				def response = RestAssuredMockMvc.get("/stubs/${it}")
			then:
				assert response.statusCode == 200
				assert Integer.valueOf(response.body.asString()) > 0
		}
	}

	@Test
	void 'should return 404 when missing stub was called'() {
		when:
			def response = RestAssuredMockMvc.get("/stubs/a:b:c:d")
		then:
			assert response.statusCode == 404
	}

	@Test
	void 'should return a list of messaging labels that can be triggered when version and classifier are passed'() {
		when:
			String response = RestAssuredMockMvc.get('/triggers').body.asString()
		then:
			def root = new JsonSlurper().parseText(response)
			assert root.'org.springframework.cloud.contract.verifier.stubs:bootService:0.0.1-SNAPSHOT:stubs'?.containsAll(["return_book_1"])
	}

	@Test
	void 'should trigger a messaging label'() {
		given:
			StubRunning stubRunning = Mockito.mock(StubRunning)
			RestAssuredMockMvc.standaloneSetup(new HttpStubsController(stubRunning), new TriggerController(stubRunning))
		when:
			def response = RestAssuredMockMvc.post("/triggers/delete_book")
		then:
			response.statusCode == 200
		and:
			Mockito.verify(stubRunning).trigger('delete_book')
	}

	@Test
	void 'should trigger a messaging label for a stub with #stubId ivy notation'() {
		given:
			StubRunning stubRunning = Mockito.mock(StubRunning)
			RestAssuredMockMvc.standaloneSetup(new HttpStubsController(stubRunning), new TriggerController(stubRunning))
		and:
			def stubIds = ['org.springframework.cloud.contract.verifier.stubs:bootService:stubs', 'org.springframework.cloud.contract.verifier.stubs:bootService', 'bootService']
		stubIds.each {
			when:
				def response = RestAssuredMockMvc.post("/triggers/$it/delete_book")
			then:
				assert response.statusCode == 200
			and:
				Mockito.verify(stubRunning).trigger(it, 'delete_book')
		}

	}

	@Test
	void 'should throw exception when trigger is missing'() {
		when:
		BDDAssertions.thenThrownBy(() -> RestAssuredMockMvc.post("/triggers/missing_label"))
		.hasMessageContaining("Exception occurred while trying to return [missing_label] label.")
		.hasMessageContaining("Available labels are")
		.hasMessageContaining("org.springframework.cloud.contract.verifier.stubs:loanIssuance:0.0.1-SNAPSHOT:stubs=[]")
		.hasMessageContaining("org.springframework.cloud.contract.verifier.stubs:bootService:0.0.1-SNAPSHOT:stubs=")
	}

}
// end::boot_usage[]
