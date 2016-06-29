/*
 *  Copyright 2013-2016 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

import org.springframework.cloud.contract.spec.Contract

Contract.make {
				request {
				method """PUT"""
				url """/fraudcheck"""
				body("""
					{
					"clientPesel":"${value(client(regex('[0-9]{10}')), server('1234567890'))}",
					"loanAmount":99999}
				"""
				)
				headers {
					header("""Content-Type""", """application/vnd.fraud.v1+json""")
				}

			}
			response {
				status 200
				body( """{
	"fraudCheckStatus": "${value(client('FRAUD'), server(regex('[A-Z]{5}')))}",
	"rejectionReason": "Amount too high"
}""")
				headers {
					 header('Content-Type': 'application/vnd.fraud.v1+json')
					}
			}

}
