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

package org.springframework.cloud.contract.verifier.converter

/**
 * Yaml representation of a {@link org.springframework.cloud.contract.spec.Contract}
 */
class YamlContract {
	public Request request = new Request()
	public Response response = new Response()

	static class Request {
		public String method
		public String url
		public Map<String, Object> headers = [:]
		public Map<String, Object> body = [:]
	}

	static class Response {
		public int status
		public Map<String, Object> headers = [:]
		public Map<String, Object> body = [:]
	}
}