/*
 *  Copyright 2013-2017 the original author or authors.
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

package org.springframework.cloud.contract.verifier.twitter.places

import org.springframework.cloud.contract.verifier.twitter.place.PairIdController
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import spock.lang.Specification

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class AcceptanceSpec extends Specification {

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
