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

package contracts.beer.messaging

org.springframework.cloud.contract.spec.Contract.make {
	description("""
Sends a positive verification message when person is eligible to get the beer

```
given:
	client is old enough
when:
	he applies for a beer
then:
	we'll send a message with a positive verification
```

""")
	// Label by means of which the output message can be triggered
	label 'accepted_verification'
	// input to the contract
	input {
		// the contract will be triggered by a method
		triggeredBy('clientIsOldEnough()')
	}
	// output message of the contract
	outputMessage {
		// destination to which the output message will be sent
		sentTo 'verifications'
		// the body of the output message
		body([
				eligible: true
		])
	}
}
