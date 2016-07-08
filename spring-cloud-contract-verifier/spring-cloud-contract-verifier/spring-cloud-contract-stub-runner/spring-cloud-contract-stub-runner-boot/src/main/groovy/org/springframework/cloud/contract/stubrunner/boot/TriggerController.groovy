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

package org.springframework.cloud.contract.stubrunner.boot

import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.contract.stubrunner.StubFinder
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

import static org.springframework.web.bind.annotation.RequestMethod.GET
import static org.springframework.web.bind.annotation.RequestMethod.POST

/**
 * @author Marcin Grzejszczak
 */
@RestController
@RequestMapping("/triggers")
@Slf4j
class TriggerController {

	private final StubFinder stubFinder

	@Autowired
	TriggerController(StubFinder stubFinder) {
		this.stubFinder = stubFinder
	}

	@RequestMapping(method = POST, path = "/{label:.*}")
	ResponseEntity<Map<String, Collection<String>>> trigger(@PathVariable String label) {
		return respond(label) {
			stubFinder.trigger(label)
		}
	}

	@RequestMapping(method = POST, path = "/{ivyNotation:.*}/{label:.*}")
	ResponseEntity<Map<String, Collection<String>>> triggerByArtifact(@PathVariable String ivyNotation, @PathVariable String label) {
		return respond(label) {
			stubFinder.trigger(ivyNotation, label)
		}
	}

	@RequestMapping(method = GET)
	Map<String, Collection<String>> labels() {
		return stubFinder.labels()
	}

	private ResponseEntity<Map<String, Collection<String>>> respond(String label, Closure closure) {
		try {
			closure()
			return ResponseEntity.ok().body()
		} catch (Exception e) {
			log.debug("Exception occurred while trying to return [$label] label", e)
			return new ResponseEntity<Map<String,Collection<String>>>(stubFinder.labels(), HttpStatus.NOT_FOUND)
		}
	}

}
