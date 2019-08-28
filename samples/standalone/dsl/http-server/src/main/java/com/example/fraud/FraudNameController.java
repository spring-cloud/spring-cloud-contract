/*
 * Copyright 2013-2019 the original author or authors.
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

package com.example.fraud;

import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

interface FraudVerifier {

	boolean isFraudByName(String name);

}

/**
 * @author Marcin Grzejszczak
 */
@RestController
class FraudNameController {

	private final FraudVerifier fraudVerifier;

	FraudNameController(FraudVerifier fraudVerifier) {
		this.fraudVerifier = fraudVerifier;
	}

	@PutMapping(value = "/frauds/name")
	public NameResponse checkByName(@RequestBody NameRequest request) {
		boolean fraud = this.fraudVerifier.isFraudByName(request.getName());
		if (fraud) {
			return new NameResponse("Sorry " + request.getName() + " but you're a fraud");
		}
		return new NameResponse(
				"Don't worry " + request.getName() + " you're not a fraud");
	}

	@GetMapping(value = "/frauds/name")
	public String checkByName(@CookieValue("name") String value,
			@CookieValue("name2") String value2) {
		return value + " " + value2;
	}

	@PutMapping(value = "/yamlfrauds/name")
	public NameResponse yamlCheckByName(@RequestBody NameRequest request) {
		return checkByName(request);
	}

	@GetMapping(value = "/yamlfrauds/name")
	public String yamlCheckByName(@CookieValue("name") String value,
			@CookieValue("name2") String value2) {
		return checkByName(value, value2);
	}

}

class NameRequest {

	private String name;

	public NameRequest(String name) {
		this.name = name;
	}

	public NameRequest() {
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

}

class NameResponse {

	private String result;

	public NameResponse(String result) {
		this.result = result;
	}

	public NameResponse() {
	}

	public String getResult() {
		return this.result;
	}

	public void setResult(String result) {
		this.result = result;
	}

}

@Component
class DefaultFraudVerifier implements FraudVerifier {

	@Override
	public boolean isFraudByName(String name) {
		return true;
	}
}