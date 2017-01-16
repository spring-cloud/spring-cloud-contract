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

package org.springframework.cloud.contract.verifier.twitter.place

import groovy.transform.TypeChecked
import groovy.util.logging.Slf4j
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

import static org.springframework.web.bind.annotation.RequestMethod.PUT

@Slf4j
@RestController
@RequestMapping('/api')
@TypeChecked
class PairIdController {

	@RequestMapping(
			value = '{pairId}',
			method = PUT,
			consumes = MediaType.APPLICATION_JSON_VALUE,
			produces = MediaType.APPLICATION_JSON_VALUE)
	String getPlacesFromTweets(@PathVariable long pairId, @RequestBody List<org.springframework.cloud.contract.verifier.twitter.place.Tweet> tweets) {
		log.info("Inside PairIdController, doing very important logic")
		if (tweets?.text != ["Gonna see you at Warsaw"]) {
			throw new IllegalArgumentException("Wrong text in tweet: ${tweets?.text}")
		}
		return """
			{
				"path" : "/api/$pairId",
				"correlationId" : 123456
			}
		"""
	}
}
