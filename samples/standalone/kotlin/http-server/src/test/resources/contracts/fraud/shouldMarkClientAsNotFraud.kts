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

package fraud

import org.springframework.cloud.contract.spec.ContractDsl.Companion.contract
import org.springframework.cloud.contract.spec.internal.HttpStatus

contract {
	request {
		method = PUT
		url = url("/fraudcheck")
		body = body(
				"client.id" to value(consumer(regex("[0-9]{10}")), producer("1234567890")),
				"loanAmount" to 123.123)
		headers {
			contentType("application/json")
		}
	}
	response {
		status = OK
		body = body(
				"fraudCheckStatus" to "OK",
				"rejection.reason" to value(consumer(null), producer(execute("assertThatRejectionReasonIsNull(\$it)")))
		)
		headers {
			contentType("application/json")
		}
	}
}
