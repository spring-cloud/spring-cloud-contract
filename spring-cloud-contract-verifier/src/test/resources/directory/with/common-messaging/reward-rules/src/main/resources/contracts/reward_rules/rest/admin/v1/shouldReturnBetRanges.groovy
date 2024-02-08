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
	description('Should return bet ranges array')
	request {
		method 'GET'
		url('/admin/v1/spin/betRanges')
	}

	response {
		status 200
		body(
				betRanges: [
						[
								betRangeId    : 3,
								fromBetPercent: -1
						],
						[
								betRangeId    : 4,
								fromBetPercent: 0
						],
						[
								betRangeId    : 1,
								fromBetPercent: 90
						],
						[
								betRangeId    : 2,
								fromBetPercent: 130
						]
				]
		)

		headers {
			contentType(applicationJsonUtf8())
		}
	}
}

