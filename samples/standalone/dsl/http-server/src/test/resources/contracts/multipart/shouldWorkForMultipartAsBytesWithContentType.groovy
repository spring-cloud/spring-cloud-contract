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

import org.springframework.cloud.contract.spec.Contract

Contract.make {
	request {
		method 'POST'
		url '/tests'
		multipart(
				[
						file1: named(
								name: value(consumer(regex(nonEmpty())), producer('filename1')),
								content: value(consumer(regex(nonEmpty())), producer('content1'))),
						file2: named(
								name: value(consumer(regex(nonEmpty())), producer('filename1')),
								content: value(c(regex(nonEmpty())), producer('content2'))),
						test : named(
								name: value(consumer(regex(nonEmpty())), producer('filename1')),
								content:
										value(c(regex(nonEmpty())), producer(fileAsBytes("test.json"))),
								contentType: value("application/json"))
				]
		)

		headers {
			contentType('multipart/form-data')
		}
	}
	response {
		status 200
		body([
				status: 'ok'
		])
		headers {
			contentType('application/json')
		}
	}
}
