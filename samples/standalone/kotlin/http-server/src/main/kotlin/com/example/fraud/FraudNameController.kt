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

import org.springframework.web.bind.annotation.CookieValue
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

interface FraudVerifier {
	fun isFraudByName(name: String): Boolean
}

data class NameRequest(val name: String)

data class NameResponse(val result: String)

@RestController
class FraudNameController constructor(val fraudVerifier: FraudVerifier) {

	@PutMapping("/frauds/name")
	fun checkByName(@RequestBody request: NameRequest): NameResponse {
		val fraud = this.fraudVerifier.isFraudByName(request.name);
		if (fraud) {
			return NameResponse("Sorry " + request.name + " but you're a fraud");
		}
		return NameResponse("Don't worry " + request.name + " you're not a fraud");
	}

	@GetMapping("/frauds/name")
	fun checkByName(@CookieValue("name") value: String,
					@CookieValue("name2") value2: String): String {
		return "$value $value2"
	}

}