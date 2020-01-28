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

org.springframework.cloud.contract.spec.Contract.make {
	// Human readable description
	description 'Should produce valid sensor data'
	// Label by means of which the output message can be triggered
	label 'sensor1'
	// input to the contract
	input {
		// the contract will be triggered by a method
		triggeredBy('createSensorData()')
	}
	// output message of the contract
	outputMessage {
		// destination to which the output message will be sent
		sentTo 'sensor-data'
		headers {
			header('contentType': 'application/json')
		}
		// the body of the output message
		body([
				id         : $(consumer(9), producer(regex("[0-9]+"))),
				temperature: "123.45"
		])
	}
}
