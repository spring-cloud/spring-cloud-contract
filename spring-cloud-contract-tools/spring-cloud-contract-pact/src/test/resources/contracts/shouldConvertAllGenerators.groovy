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
	request {
		method 'POST'
		url '/'
		body([
				someInteger     : $(c(anyInteger()), p(1234567890)),
				someDecimal     : $(c(anyDouble()), p(123.123)),
				someHex         : $(c(anyHex()), p('DEADC0DE')),
				someAlphaNumeric:
						$(c(anyAlphaNumeric()), p('Some alpha numeric string with 1234567890')),
				someUUID        : $(c(anyUuid()), p('00000000-0000-0000-0000-000000000000')),
				someDate        : $(c(anyDate()), p('2018-03-26')),
				someTime        : $(c(anyTime()), p('13:37:00')),
				someDateTime    : $(c(anyDateTime()), p('2018-03-26 13:37:00')),
				someBoolean     : $(c(anyBoolean()), p('true')),
				someRegex       : $(c(regex('[0-9]{10}')), p(1234567890))
		])
		headers {
			contentType('application/json')
		}
	}
	response {
		status OK()
		body([
				someInteger     : $(c(1234567890), p(anyInteger())),
				someDecimal     : $(c(123.123), p(anyDouble())),
				someHex         : $(c('DEADC0DE'), p(anyHex())),
				someAlphaNumeric:
						$(c('Some alpha numeric string with 1234567890'), p(anyAlphaNumeric())),
				someUUID        : $(c('00000000-0000-0000-0000-000000000000'), p(anyUuid())),
				someDate        : $(c('2018-03-26'), p(anyDate())),
				someTime        : $(c('13:37:00'), p(anyTime())),
				someDateTime    : $(c('2018-03-26 13:37:00'), p(anyDateTime())),
				someBoolean     : $(c('true'), p(anyBoolean())),
				someRegex       : $(c(1234567890), p(regex('[0-9]{10}')))
		])
		headers {
			contentType('application/json')
		}
	}
	metadata([
			pact: [
					providerStates:
					[
						[
							name: "someState1",
							params: [
								id: 1,
								value: "someValue1"
							]
						],
						[
								name: "someState2",
								params: [
										id: 2,
										value: "someValue2"
								]
						]
					]
			]
	])
}
