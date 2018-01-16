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

import groovy.transform.CompileStatic

/**
 * YAML representation of a {@link org.springframework.cloud.contract.spec.Contract}
 * 
 * @since 1.2.1
 * @author Marcin Grzejszczak
 */
@CompileStatic
class YamlContract {
	public Request request
	public Response response
	public Input input
	public OutputMessage outputMessage
	public String description
	public String label
	public String name
	public Integer priority
	public boolean ignored

	@CompileStatic
	static class Request {
		public String method
		public String url
		public Map<String, String> queryParameters = [:]
		public Map<String, Object> headers = [:]
		public Object body
		public StubMatchers matchers = new StubMatchers()
	}

	@CompileStatic
	static class StubMatchers {
		public List<BodyStubMatcher> body = []
		public List<StubHeaderMatcher> headers = []
	}

	@CompileStatic
	static class BodyStubMatcher {
		public String path
		public StubMatcherType type
		public String value
	}

	@CompileStatic
	static class BodyTestMatcher {
		public String path
		public TestMatcherType type
		public String value
		public Integer minOccurrence
		public Integer maxOccurrence
	}

	@CompileStatic
	static class StubHeaderMatcher {
		public String key
		public String regex
	}

	@CompileStatic
	static class TestHeaderMatcher {
		public String key
		public String regex
		public String command
	}

	@CompileStatic
	static enum StubMatcherType {
		by_date, by_time, by_timestamp, by_regex, by_equality
	}

	@CompileStatic
	static enum TestMatcherType {
		by_date, by_time, by_timestamp, by_regex, by_equality, by_type, by_command
	}

	@CompileStatic
	static class Response {
		public int status
		public Map<String, Object> headers = [:]
		public Object body
		public TestMatchers matchers = new TestMatchers()
	}

	@CompileStatic
	static class TestMatchers {
		public List<BodyTestMatcher> body = []
		public List<TestHeaderMatcher> headers = []
	}

	@CompileStatic
	static class Input {
		public String messageFrom
		public String triggeredBy
		public Map<String, Object> messageHeaders = [:]
		public Object messageBody
		public String assertThat
		public StubMatchers matchers = new StubMatchers()
	}

	@CompileStatic
	static class OutputMessage {
		public String sentTo
		public Map<String, Object> headers = [:]
		public Object body
		public String assertThat
		public TestMatchers matchers = new TestMatchers()
	}
}