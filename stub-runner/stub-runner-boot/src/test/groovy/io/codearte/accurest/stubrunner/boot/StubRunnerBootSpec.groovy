package io.codearte.accurest.stubrunner.boot

import com.jayway.restassured.module.mockmvc.RestAssuredMockMvc
import groovy.json.JsonSlurper
import io.codearte.accurest.stubrunner.StubRunning
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.SpringApplicationContextLoader
import org.springframework.cloud.stream.annotation.EnableBinding
import org.springframework.context.annotation.Configuration
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

/**
 * @author Marcin Grzejszczak
 */
// tag::boot_usage[]
@ContextConfiguration(classes = [StubRunnerBootSpec, StubRunnerBoot], loader = SpringApplicationContextLoader)
@EnableBinding
@Configuration
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
			root.'io.codearte.accurest.stubs:streamService:0.0.1-SNAPSHOT:stubs' instanceof Integer
	}

	def 'should return a port on which a [#stubId] stub is running'() {
		when:
			def response = RestAssuredMockMvc.get("/stubs/${stubId}")
		then:
			response.statusCode == 200
			response.body.as(Integer) > 0
		where:
			stubId << ['io.codearte.accurest.stubs:streamService:+:stubs',
					   'io.codearte.accurest.stubs:streamService:0.0.1-SNAPSHOT:stubs',
					   'io.codearte.accurest.stubs:streamService:+',
					   'io.codearte.accurest.stubs:streamService',
					   'streamService']
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
			root.'io.codearte.accurest.stubs:streamService:0.0.1-SNAPSHOT:stubs'?.containsAll(["delete_book","return_book_1","return_book_2"])
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
			stubId << ['io.codearte.accurest.stubs:streamService:stubs', 'io.codearte.accurest.stubs:streamService', 'streamService']
	}

	def 'should return when trigger is missing'() {
		when:
			def response = RestAssuredMockMvc.post("/triggers/missing_label")
		then:
			response.statusCode == 404
			def root = new JsonSlurper().parseText(response.body.asString())
			root.'io.codearte.accurest.stubs:streamService:0.0.1-SNAPSHOT:stubs'?.containsAll(["delete_book","return_book_1","return_book_2"])
	}

}
// end::boot_usage[]
