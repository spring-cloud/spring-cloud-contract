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

import org.springframework.cloud.contract.spec.Contract

Contract.make {
	description("""
User's information should be update if has appropriate age
""")
	request {
		method 'POST'
		url '/test/updateUserInfo'
		body([
				userId   : 123,
				age      : 25,
				firstName: "asd",
				lastName : "asd"
		])
		stubMatchers {
			jsonPath('$.userId', byRegex("[1-9]{1}([0-9]{7})"))
			jsonPath('$.age', byRegex("(1[89]|[2-9][0-9])"))
			jsonPath('$.firstName', byRegex("[a-zA-Z]{2,20}"))
			jsonPath('$.lastName', byRegex("[a-zA-Z]{2,20}"))
		}
		headers {
			contentType(applicationJson())
		}
	}
	response {
		status 200
		body([
				userId   : fromRequest().body("userId"),
				age      : fromRequest().body("age"),
				firstName: fromRequest().body("firstName"),
				lastName : fromRequest().body("lastName")
		])
		testMatchers {
			jsonPath('$.userId', byEquality())
			jsonPath('$.age', byEquality())
			jsonPath('$.firstName', byEquality())
			jsonPath('$.lastName', byEquality())
		}
		headers {
			contentType(applicationJson())
		}
	}
}
