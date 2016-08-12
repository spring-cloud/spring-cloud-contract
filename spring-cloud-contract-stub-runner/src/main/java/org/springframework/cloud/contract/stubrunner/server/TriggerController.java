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

package org.springframework.cloud.contract.stubrunner.server;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.contract.stubrunner.StubFinder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Marcin Grzejszczak
 */
@RestController
@RequestMapping("/triggers")
public class TriggerController {
	
	private static final Logger log = LoggerFactory.getLogger(TriggerController.class);

	private final StubFinder stubFinder;

	@Autowired
	public TriggerController(StubFinder stubFinder) {
		this.stubFinder = stubFinder;
	}

	@PostMapping("/{label:.*}")
	public ResponseEntity<Map<String, Collection<String>>> trigger(@PathVariable String label) {
		try {
			stubFinder.trigger(label);
			return ResponseEntity.ok().body(Collections.<String, Collection<String>>emptyMap());
		} catch (Exception e) {
			if (log.isDebugEnabled()) {
				log.debug("Exception occurred while trying to return " + label + " label", e);
			}
			return new ResponseEntity<>(stubFinder.labels(), HttpStatus.NOT_FOUND);
		}
	}

	@PostMapping("/{ivyNotation:.*}/{label:.*}")
	public ResponseEntity<Map<String, Collection<String>>> triggerByArtifact(@PathVariable String ivyNotation, @PathVariable String label) {
		try {
			stubFinder.trigger(ivyNotation, label);
			return ResponseEntity.ok().body(Collections.<String, Collection<String>>emptyMap());
		} catch (Exception e) {
			if (log.isDebugEnabled()) {
				log.debug("Exception occurred while trying to return " + label + " label", e);
			}
			return new ResponseEntity<>(stubFinder.labels(), HttpStatus.NOT_FOUND);
		}
	}

	@GetMapping
	public Map<String, Collection<String>> labels() {
		return stubFinder.labels();
	}

}
