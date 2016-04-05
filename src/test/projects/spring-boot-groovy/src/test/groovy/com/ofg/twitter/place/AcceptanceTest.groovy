package com.ofg.twitter.place

import com.ofg.twitter.place.PairIdController
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import spock.lang.Specification

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class AcceptanceTest extends Specification {

	def "should have controller up and running"() {
		given:
			MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new PairIdController()).build()
		expect:
			mockMvc.perform(put("/api/${1}").
					contentType(MediaType.APPLICATION_JSON).
					content("""[{"text":"Gonna see you at Warsaw"}]""")).
					andExpect(status().isOk())
	}
}
