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
		method 'PUT'
		url $(consumer(regex('^/api/[0-9]{2}$')), producer('/api/12'))
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
			header 'Content-Type': $(consumer('application/json'), producer(regex('application/json.*')))
			header 'Location': $(consumer('https://localhost:8080'), producer(execute('isEmpty($it)')))
		}
		body(
				path: $(consumer('/api/12'), producer(regex('^/api/[0-9]{2}$'))),
				correlationId: $(consumer('1223456'), producer(execute('isProperCorrelationId($it)')))
		)
		status OK()
	}
}
