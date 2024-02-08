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

org.springframework.cloud.contract.spec.Contract.make {
	description 'issue #650'
	label 'trigger'
	input {
		triggeredBy('createNewPerson()')
	}
	outputMessage {
		sentTo 'personEventsTopic'
		headers {
			[
					header('contentType': 'application/json'),
					header('type': 'person'),
					header('eventType': 'PersonChangedEvent'),
					header('customerId': $(producer(regex(uuid()))))
			]
		}
		body([
				"type"      : 'CREATED',
				"personId"  : $(producer(regex(uuid())),
						consumer('0fd552ba-8043-42da-ab97-4fc77e1057c9')),
				"userId"    : $(producer(optional(regex(uuid()))),
						consumer('f043ccf1-0b72-423b-ad32-4ef123718897')),
				"firstName" : $(regex(nonEmpty())),
				"middleName": $(optional(regex(nonEmpty()))),
				"lastName"  : $(regex(nonEmpty())),
				"version"   : $(producer(regex(number())), consumer(0l)),
				"uid"       : $(producer(regex(uuid())))
		])
	}
}
