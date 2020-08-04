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

package contracts

import org.springframework.cloud.contract.spec.Contract

[
	Contract.make {
		request {
			name "should count all frauds"
			method GET()
			url '/frauds'
		}
		response {
			status OK()
			body([
				count: $(regex("[2-9][0-9][0-9]").asInteger())
			])
			headers {
				contentType("application/json")
			}
		}
	},
	// tag::metadata[]
	Contract.make {
		request {
			method GET()
			url '/drunks'
		}
		response {
			status OK()
			body([
				count: 100
			])
			headers {
				contentType("application/json")
			}
		}
		metadata([wiremock: '''\
	{
		"response" : {
			"fixedDelayMilliseconds": 2000
		}
	}
'''
		])
	}
	// end::metadata[]
]
