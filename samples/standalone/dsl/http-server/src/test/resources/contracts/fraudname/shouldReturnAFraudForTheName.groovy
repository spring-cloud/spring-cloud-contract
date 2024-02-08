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

package contracts.fraudname

org.springframework.cloud.contract.spec.Contract.make {
	// highest priority
	priority(1)
	request {
		method PUT()
		url '/frauds/name'
		body([
				name: "fraud"
		])
		headers {
			contentType("application/json")
		}
	}
	response {
		status OK()
		body([
				result: "Sorry ${fromRequest().body('$.name')} but you're a fraud"
		])
		headers {
			header(contentType(), "${fromRequest().header(contentType())}")
		}
	}
}
