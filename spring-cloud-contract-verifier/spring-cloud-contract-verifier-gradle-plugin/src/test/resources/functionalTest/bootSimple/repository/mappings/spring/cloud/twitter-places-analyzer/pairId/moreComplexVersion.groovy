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

import org.springframework.cloud.contract.verifier.dsl.Contract

Contract.make {
	request {
		method 'PUT'
		url $(client(regex('^/api/[0-9]{2}$')), server('/api/12'))
		headers {
			header 'Content-Type': 'application/json'
		}
		body '''\
	[{
		"text": "Gonna see you at Warsaw"
	}]
'''
	}
	response {
		headers {
			header 'Content-Type': $(client('application/json'), server(regex('application/json.*')))
			header 'Location': $(client('https://localhost:8080'), server(execute('isEmpty($it)')))
		}
		body (
			 path: $(client('/api/12'), server(regex('^/api/[0-9]{2}$'))),
			 correlationId: $(client('1223456'), server(execute('isProperCorrelationId($it)')))
		)
		status 200
	}
}