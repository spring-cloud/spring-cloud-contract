/*
 *  Copyright 2013-2016 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.springframework.cloud.contract.stubrunner.server

import com.jayway.restassured.module.mockmvc.RestAssuredMockMvc
import groovy.json.JsonSlurper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.IntegrationTest
import org.springframework.boot.test.context.SpringBootContextLoader
import org.springframework.cloud.contract.stubrunner.StubRunning
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification
/**
 * @author Marcin Grzejszczak
 */
// tag::boot_usage[]
@ContextConfiguration(classes = StubRunnerBoot, loader = SpringBootContextLoader)
@IntegrationTest("spring.cloud.zookeeper.enabled=false")
@ActiveProfiles("test")
class StubRunnerBootSpec extends Specification {

	@Autowired StubRunning stubRunning

	def setup() {
		RestAssuredMockMvc.standaloneSetup(new HttpStubsController(stubRunning),
				new TriggerController(stubRunning))
	}

	def 'should return a list of running stub servers in "full ivy:port" notation'() {
		when:
			String response = RestAssuredMockMvc.get('/stubs').body.asString()
		then:
			def root = new JsonSlurper().parseText(response)
			root.'org.springframework.cloud.contract.verifier.stubs:bootService:0.0.1-SNAPSHOT:stubs' instanceof Integer
	}

	def 'should return a port on which a [#stubId] stub is running'() {
		when:
			def response = RestAssuredMockMvc.get("/stubs/${stubId}")
		then:
			response.statusCode == 200
			response.body.as(Integer) > 0
		where:
			stubId << ['org.springframework.cloud.contract.verifier.stubs:bootService:+:stubs',
					   'org.springframework.cloud.contract.verifier.stubs:bootService:0.0.1-SNAPSHOT:stubs',
					   'org.springframework.cloud.contract.verifier.stubs:bootService:+',
					   'org.springframework.cloud.contract.verifier.stubs:bootService',
					   'bootService']
	}

	def 'should return 404 when missing stub was called'() {
		when:
			def response = RestAssuredMockMvc.get("/stubs/a:b:c:d")
		then:
			response.statusCode == 404
	}

	def 'should return a list of messaging labels that can be triggered when version and classifier are passed'() {
		when:
			String response = RestAssuredMockMvc.get('/triggers').body.asString()
		then:
			def root = new JsonSlurper().parseText(response)
			root.'org.springframework.cloud.contract.verifier.stubs:bootService:0.0.1-SNAPSHOT:stubs'?.containsAll(["delete_book","return_book_1","return_book_2"])
	}

	def 'should trigger a messaging label'() {
		given:
			StubRunning stubRunning = Mock()
			RestAssuredMockMvc.standaloneSetup(new HttpStubsController(stubRunning), new TriggerController(stubRunning))
		when:
			def response = RestAssuredMockMvc.post("/triggers/delete_book")
		then:
			response.statusCode == 200
		and:
			1 * stubRunning.trigger('delete_book')
	}

	def 'should trigger a messaging label for a stub with [#stubId] ivy notation'() {
		given:
			StubRunning stubRunning = Mock()
			RestAssuredMockMvc.standaloneSetup(new HttpStubsController(stubRunning), new TriggerController(stubRunning))
		when:
			def response = RestAssuredMockMvc.post("/triggers/$stubId/delete_book")
		then:
			response.statusCode == 200
		and:
			1 * stubRunning.trigger(stubId, 'delete_book')
		where:
			stubId << ['org.springframework.cloud.contract.verifier.stubs:bootService:stubs', 'org.springframework.cloud.contract.verifier.stubs:bootService', 'bootService']
	}

	def 'should return when trigger is missing'() {
		when:
			def response = RestAssuredMockMvc.post("/triggers/missing_label")
		then:
			response.statusCode == 404
			def root = new JsonSlurper().parseText(response.body.asString())
			root.'org.springframework.cloud.contract.verifier.stubs:bootService:0.0.1-SNAPSHOT:stubs'?.containsAll(["delete_book","return_book_1","return_book_2"])
	}

}
// end::boot_usage[]
