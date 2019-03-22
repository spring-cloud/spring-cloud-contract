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

org.springframework.cloud.contract.spec.Contract.make {
	// Human readable description
	description 'Sends an order message'
	// Label by means of which the output message can be triggered
	label 'send_order2'
	// input to the contract
	input {
		// the contract will be triggered by a method
		triggeredBy('orderTrigger()')
	}
	// output message of the contract
	outputMessage {
		// destination to which the output message will be sent
		sentTo('orders')
		// any headers for the output message
		headers {
			header('contentType': 'application/json')
		}
		// the body of the output message
		body(
				orderId: value(
						consumer('40058c70-891c-4176-a033-f70bad0c5f77'),
						producer(regex('([0-9|a-f]*-*)*'))),
				description: "This is the order description"
		)
	}
}
