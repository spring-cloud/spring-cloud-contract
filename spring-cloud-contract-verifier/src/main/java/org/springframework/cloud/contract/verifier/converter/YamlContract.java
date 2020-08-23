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

package org.springframework.cloud.contract.verifier.converter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.cloud.contract.spec.Contract;

/**
 * YAML representation of a {@link Contract}.
 *
 * @author Marcin Grzejszczak
 * @author Tim Ysewyn
 * @since 1.2.1
 */
public class YamlContract {

	public Request request;

	public Response response;

	public Input input;

	public OutputMessage outputMessage;

	public String description;

	public String label;

	public String name;

	public Integer priority;

	public boolean ignored;

	public boolean inProgress;

	public Map<String, Object> metadata = new HashMap<>();

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		YamlContract that = (YamlContract) o;
		return ignored == that.ignored && inProgress == that.inProgress
				&& Objects.equals(this.request, that.request)
				&& Objects.equals(this.response, that.response)
				&& Objects.equals(this.input, that.input)
				&& Objects.equals(this.outputMessage, that.outputMessage)
				&& Objects.equals(this.description, that.description)
				&& Objects.equals(this.label, that.label)
				&& Objects.equals(this.name, that.name)
				&& Objects.equals(this.priority, that.priority)
				&& this.metadata.equals(that.metadata);
	}

	@Override
	public int hashCode() {
		return Objects.hash(request, response, input, outputMessage, description, label,
				name, priority, ignored, inProgress, metadata);
	}

	public static class Request {

		public String method;

		public String url;

		public String urlPath;

		public Map<String, Object> queryParameters = new LinkedHashMap<String, Object>();

		public Map<String, Object> headers = new LinkedHashMap<String, Object>();

		public Map<String, Object> cookies = new LinkedHashMap<String, Object>();

		public Object body;

		public String bodyFromFile;

		public String bodyFromFileAsBytes;

		public StubMatchers matchers = new StubMatchers();

		public Multipart multipart;

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || getClass() != o.getClass()) {
				return false;
			}
			Request request = (Request) o;
			return Objects.equals(method, request.method)
					&& Objects.equals(url, request.url)
					&& Objects.equals(urlPath, request.urlPath)
					&& Objects.equals(queryParameters, request.queryParameters)
					&& Objects.equals(headers, request.headers)
					&& Objects.equals(cookies, request.cookies)
					&& Objects.equals(body, request.body)
					&& Objects.equals(bodyFromFile, request.bodyFromFile)
					&& Objects.equals(bodyFromFileAsBytes, request.bodyFromFileAsBytes)
					&& Objects.equals(matchers, request.matchers)
					&& Objects.equals(multipart, request.multipart);
		}

		@Override
		public int hashCode() {
			return Objects.hash(method, url, urlPath, queryParameters, headers, cookies,
					body, bodyFromFile, bodyFromFileAsBytes, matchers, multipart);
		}

		@Override
		public String toString() {
			return "Request{" + "method='" + method + '\'' + ", url='" + url + '\''
					+ ", urlPath='" + urlPath + '\'' + ", queryParameters="
					+ queryParameters + ", headers=" + headers + ", cookies=" + cookies
					+ ", body=" + body + ", bodyFromFile='" + bodyFromFile + '\''
					+ ", bodyFromFileAsBytes='" + bodyFromFileAsBytes + '\''
					+ ", matchers=" + matchers + ", multipart=" + multipart + '}';
		}

	}

	public static class Multipart {

		public Map<String, String> params = new LinkedHashMap<String, String>();

		public List<Named> named = new ArrayList<Named>();

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || getClass() != o.getClass()) {
				return false;
			}
			Multipart multipart = (Multipart) o;
			return Objects.equals(params, multipart.params)
					&& Objects.equals(named, multipart.named);
		}

		@Override
		public int hashCode() {
			return Objects.hash(params, named);
		}

		@Override
		public String toString() {
			return "Multipart{" + "params=" + params + ", named=" + named + '}';
		}

	}

	public static class Named {

		public String paramName;

		public String fileName;

		public String fileContent;

		public String fileContentAsBytes;

		public String fileContentFromFileAsBytes;

		public String contentType;

		public String fileNameCommand;

		public String fileContentCommand;

		public String contentTypeCommand;

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || getClass() != o.getClass()) {
				return false;
			}
			Named named = (Named) o;
			return Objects.equals(paramName, named.paramName)
					&& Objects.equals(fileName, named.fileName)
					&& Objects.equals(fileContent, named.fileContent)
					&& Objects.equals(fileContentAsBytes, named.fileContentAsBytes)
					&& Objects.equals(fileContentFromFileAsBytes,
							named.fileContentFromFileAsBytes)
					&& Objects.equals(contentType, named.contentType)
					&& Objects.equals(fileNameCommand, named.fileNameCommand)
					&& Objects.equals(fileContentCommand, named.fileContentCommand)
					&& Objects.equals(contentTypeCommand, named.contentTypeCommand);
		}

		@Override
		public int hashCode() {
			return Objects.hash(paramName, fileName, fileContent, fileContentAsBytes,
					fileContentFromFileAsBytes, contentType, fileNameCommand,
					fileContentCommand, contentTypeCommand);
		}

		@Override
		public String toString() {
			return "Named{" + "paramName='" + paramName + '\'' + ", fileName='" + fileName
					+ '\'' + ", fileContent='" + fileContent + '\''
					+ ", fileContentAsBytes='" + fileContentAsBytes + '\''
					+ ", fileContentFromFileAsBytes='" + fileContentFromFileAsBytes + '\''
					+ ", contentType='" + contentType + '\'' + ", fileNameCommand='"
					+ fileNameCommand + '\'' + ", fileContentCommand='"
					+ fileContentCommand + '\'' + ", contentTypeCommand='"
					+ contentTypeCommand + '\'' + '}';
		}

	}

	public static class StubMatchers {

		public KeyValueMatcher url;

		public List<BodyStubMatcher> body = new ArrayList<BodyStubMatcher>();

		public List<KeyValueMatcher> headers = new ArrayList<KeyValueMatcher>();

		public List<QueryParameterMatcher> queryParameters = new ArrayList<QueryParameterMatcher>();

		public List<KeyValueMatcher> cookies = new ArrayList<KeyValueMatcher>();

		public MultipartStubMatcher multipart;

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || getClass() != o.getClass()) {
				return false;
			}
			StubMatchers that = (StubMatchers) o;
			return Objects.equals(url, that.url) && Objects.equals(body, that.body)
					&& Objects.equals(headers, that.headers)
					&& Objects.equals(queryParameters, that.queryParameters)
					&& Objects.equals(cookies, that.cookies)
					&& Objects.equals(multipart, that.multipart);
		}

		@Override
		public int hashCode() {
			return Objects.hash(url, body, headers, queryParameters, cookies, multipart);
		}

		@Override
		public String toString() {
			return "StubMatchers{" + "url=" + url + ", body=" + body + ", headers="
					+ headers + ", queryParameters=" + queryParameters + ", cookies="
					+ cookies + ", multipart=" + multipart + '}';
		}

	}

	public enum MatchingType {

		equal_to, containing, matching, not_matching, equal_to_json, equal_to_xml, absent, binary_equal_to;

		static MatchingType from(String string) {
			return Arrays.stream(values())
					.filter(matchingType -> matchingType.name().replace("_", "")
							.equalsIgnoreCase(string.toLowerCase().replace("_", "")))
					.findFirst().orElse(null);
		}

	}

	public static class BodyStubMatcher {

		public String path;

		public StubMatcherType type;

		public String value;

		public PredefinedRegex predefined;

		public Integer minOccurrence;

		public Integer maxOccurrence;

		public RegexType regexType;

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || getClass() != o.getClass()) {
				return false;
			}
			BodyStubMatcher that = (BodyStubMatcher) o;
			return Objects.equals(path, that.path) && type == that.type
					&& Objects.equals(value, that.value) && predefined == that.predefined
					&& Objects.equals(minOccurrence, that.minOccurrence)
					&& Objects.equals(maxOccurrence, that.maxOccurrence)
					&& regexType == that.regexType;
		}

		@Override
		public int hashCode() {
			return Objects.hash(path, type, value, predefined, minOccurrence,
					maxOccurrence, regexType);
		}

		@Override
		public String toString() {
			return "BodyStubMatcher{" + "path='" + path + '\'' + ", type=" + type
					+ ", value='" + value + '\'' + ", predefined=" + predefined
					+ ", minOccurrence=" + minOccurrence + ", maxOccurrence="
					+ maxOccurrence + ", regexType=" + regexType + '}';
		}

	}

	public enum RegexType {

		as_integer, as_double, as_float, as_long, as_short, as_boolean, as_string;

	}

	public static class MultipartStubMatcher {

		public List<KeyValueMatcher> params = new ArrayList<KeyValueMatcher>();

		public List<MultipartNamedStubMatcher> named = new ArrayList<MultipartNamedStubMatcher>();

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || getClass() != o.getClass()) {
				return false;
			}
			MultipartStubMatcher that = (MultipartStubMatcher) o;
			return Objects.equals(params, that.params)
					&& Objects.equals(named, that.named);
		}

		@Override
		public int hashCode() {
			return Objects.hash(params, named);
		}

		@Override
		public String toString() {
			return "MultipartStubMatcher{" + "params=" + params + ", named=" + named
					+ '}';
		}

	}

	public static class MultipartNamedStubMatcher {

		public String paramName;

		public ValueMatcher fileName;

		public ValueMatcher fileContent;

		public ValueMatcher contentType;

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || getClass() != o.getClass()) {
				return false;
			}
			MultipartNamedStubMatcher that = (MultipartNamedStubMatcher) o;
			return Objects.equals(paramName, that.paramName)
					&& Objects.equals(fileName, that.fileName)
					&& Objects.equals(fileContent, that.fileContent)
					&& Objects.equals(contentType, that.contentType);
		}

		@Override
		public int hashCode() {
			return Objects.hash(paramName, fileName, fileContent, contentType);
		}

		@Override
		public String toString() {
			return "MultipartNamedStubMatcher{" + "paramName='" + paramName + '\''
					+ ", fileName=" + fileName + ", fileContent=" + fileContent
					+ ", contentType=" + contentType + '}';
		}

	}

	public static class ValueMatcher {

		public String regex;

		public PredefinedRegex predefined;

		public ValueMatcher() {
		}

		public ValueMatcher(String regex) {
			this.regex = regex;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || getClass() != o.getClass()) {
				return false;
			}
			ValueMatcher that = (ValueMatcher) o;
			return Objects.equals(regex, that.regex) && predefined == that.predefined;
		}

		@Override
		public int hashCode() {
			return Objects.hash(regex, predefined);
		}

		@Override
		public String toString() {
			return "ValueMatcher{" + "regex='" + regex + '\'' + ", predefined="
					+ predefined + '}';
		}

	}

	public static class BodyTestMatcher {

		public String path;

		public TestMatcherType type;

		public String value;

		public Integer minOccurrence;

		public Integer maxOccurrence;

		public PredefinedRegex predefined;

		public RegexType regexType;

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || getClass() != o.getClass()) {
				return false;
			}
			BodyTestMatcher that = (BodyTestMatcher) o;
			return Objects.equals(path, that.path) && type == that.type
					&& Objects.equals(value, that.value)
					&& Objects.equals(minOccurrence, that.minOccurrence)
					&& Objects.equals(maxOccurrence, that.maxOccurrence)
					&& predefined == that.predefined && regexType == that.regexType;
		}

		@Override
		public int hashCode() {
			return Objects.hash(path, type, value, minOccurrence, maxOccurrence,
					predefined, regexType);
		}

		@Override
		public String toString() {
			return "BodyTestMatcher{" + "path='" + path + '\'' + ", type=" + type
					+ ", value='" + value + '\'' + ", minOccurrence=" + minOccurrence
					+ ", maxOccurrence=" + maxOccurrence + ", predefined=" + predefined
					+ ", regexType=" + regexType + '}';
		}

	}

	public static class KeyValueMatcher {

		public String key;

		public String regex;

		public PredefinedRegex predefined;

		public String command;

		public RegexType regexType;

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || getClass() != o.getClass()) {
				return false;
			}
			KeyValueMatcher that = (KeyValueMatcher) o;
			return Objects.equals(key, that.key) && Objects.equals(regex, that.regex)
					&& predefined == that.predefined
					&& Objects.equals(command, that.command)
					&& regexType == that.regexType;
		}

		@Override
		public int hashCode() {
			return Objects.hash(key, regex, predefined, command, regexType);
		}

		@Override
		public String toString() {
			return "KeyValueMatcher{" + "key='" + key + '\'' + ", regex='" + regex + '\''
					+ ", predefined=" + predefined + ", command='" + command + '\''
					+ ", regexType=" + regexType + '}';
		}

	}

	public static class HeadersMatcher extends KeyValueMatcher {

	}

	public static class QueryParameterMatcher {

		public String key;

		public MatchingType type;

		public Object value;

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || getClass() != o.getClass()) {
				return false;
			}
			QueryParameterMatcher that = (QueryParameterMatcher) o;
			return Objects.equals(key, that.key) && type == that.type
					&& Objects.equals(value, that.value);
		}

		@Override
		public int hashCode() {
			return Objects.hash(key, type, value);
		}

		@Override
		public String toString() {
			return "QueryParameterMatcher{" + "key='" + key + '\'' + ", type=" + type
					+ ", value=" + value + '}';
		}

	}

	public static class TestHeaderMatcher {

		public String key;

		public String regex;

		public String command;

		public PredefinedRegex predefined;

		public RegexType regexType;

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || getClass() != o.getClass()) {
				return false;
			}
			TestHeaderMatcher that = (TestHeaderMatcher) o;
			return Objects.equals(key, that.key) && Objects.equals(regex, that.regex)
					&& Objects.equals(command, that.command)
					&& predefined == that.predefined && regexType == that.regexType;
		}

		@Override
		public int hashCode() {
			return Objects.hash(key, regex, command, predefined, regexType);
		}

		@Override
		public String toString() {
			return "TestHeaderMatcher{" + "key='" + key + '\'' + ", regex='" + regex
					+ '\'' + ", command='" + command + '\'' + ", predefined=" + predefined
					+ ", regexType=" + regexType + '}';
		}

	}

	public static class TestCookieMatcher {

		public String key;

		public String regex;

		public String command;

		public PredefinedRegex predefined;

		public RegexType regexType;

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || getClass() != o.getClass()) {
				return false;
			}
			TestCookieMatcher that = (TestCookieMatcher) o;
			return Objects.equals(key, that.key) && Objects.equals(regex, that.regex)
					&& Objects.equals(command, that.command)
					&& predefined == that.predefined && regexType == that.regexType;
		}

		@Override
		public int hashCode() {
			return Objects.hash(key, regex, command, predefined, regexType);
		}

		@Override
		public String toString() {
			return "TestCookieMatcher{" + "key='" + key + '\'' + ", regex='" + regex
					+ '\'' + ", command='" + command + '\'' + ", predefined=" + predefined
					+ ", regexType=" + regexType + '}';
		}

	}

	public enum PredefinedRegex {

		only_alpha_unicode, number, any_double, any_boolean, ip_address, hostname, email, url, uuid, iso_date, iso_date_time, iso_time, iso_8601_with_offset, non_empty, non_blank;

	}

	public enum StubMatcherType {

		by_date, by_time, by_timestamp, by_regex, by_equality, by_type, by_null;

	}

	public enum TestMatcherType {

		by_date, by_time, by_timestamp, by_regex, by_equality, by_type, by_command, by_null;

	}

	public static class Response {

		public int status;

		public Map<String, Object> headers = new LinkedHashMap<String, Object>();

		public Map<String, Object> cookies = new LinkedHashMap<String, Object>();

		public Object body;

		public String bodyFromFile;

		public String bodyFromFileAsBytes;

		public TestMatchers matchers = new TestMatchers();

		public Boolean async;

		public Integer fixedDelayMilliseconds;

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || getClass() != o.getClass()) {
				return false;
			}
			Response response = (Response) o;
			return status == response.status && Objects.equals(headers, response.headers)
					&& Objects.equals(cookies, response.cookies)
					&& Objects.equals(body, response.body)
					&& Objects.equals(bodyFromFile, response.bodyFromFile)
					&& Objects.equals(bodyFromFileAsBytes, response.bodyFromFileAsBytes)
					&& Objects.equals(matchers, response.matchers)
					&& Objects.equals(async, response.async) && Objects.equals(
							fixedDelayMilliseconds, response.fixedDelayMilliseconds);
		}

		@Override
		public int hashCode() {
			return Objects.hash(status, headers, cookies, body, bodyFromFile,
					bodyFromFileAsBytes, matchers, async, fixedDelayMilliseconds);
		}

		@Override
		public String toString() {
			return "Response{" + "status=" + status + ", headers=" + headers
					+ ", cookies=" + cookies + ", body=" + body + ", bodyFromFile='"
					+ bodyFromFile + '\'' + ", bodyFromFileAsBytes='"
					+ bodyFromFileAsBytes + '\'' + ", matchers=" + matchers + ", async="
					+ async + ", fixedDelayMilliseconds=" + fixedDelayMilliseconds + '}';
		}

	}

	public static class TestMatchers {

		public List<BodyTestMatcher> body = new ArrayList<BodyTestMatcher>();

		public List<TestHeaderMatcher> headers = new ArrayList<TestHeaderMatcher>();

		public List<TestCookieMatcher> cookies = new ArrayList<TestCookieMatcher>();

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || getClass() != o.getClass()) {
				return false;
			}
			TestMatchers that = (TestMatchers) o;
			return Objects.equals(body, that.body)
					&& Objects.equals(headers, that.headers)
					&& Objects.equals(cookies, that.cookies);
		}

		@Override
		public int hashCode() {
			return Objects.hash(body, headers, cookies);
		}

		@Override
		public String toString() {
			return "TestMatchers{" + "body=" + body + ", headers=" + headers
					+ ", cookies=" + cookies + '}';
		}

	}

	public static class Input {

		public String messageFrom;

		public String triggeredBy;

		public Map<String, Object> messageHeaders = new LinkedHashMap<String, Object>();

		public Object messageBody;

		public String messageBodyFromFile;

		public String messageBodyFromFileAsBytes;

		public String assertThat;

		public StubMatchers matchers = new StubMatchers();

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || getClass() != o.getClass()) {
				return false;
			}
			Input input = (Input) o;
			return Objects.equals(messageFrom, input.messageFrom)
					&& Objects.equals(triggeredBy, input.triggeredBy)
					&& Objects.equals(messageHeaders, input.messageHeaders)
					&& Objects.equals(messageBody, input.messageBody)
					&& Objects.equals(messageBodyFromFile, input.messageBodyFromFile)
					&& Objects.equals(messageBodyFromFileAsBytes,
							input.messageBodyFromFileAsBytes)
					&& Objects.equals(assertThat, input.assertThat)
					&& Objects.equals(matchers, input.matchers);
		}

		@Override
		public int hashCode() {
			return Objects.hash(messageFrom, triggeredBy, messageHeaders, messageBody,
					messageBodyFromFile, messageBodyFromFileAsBytes, assertThat,
					matchers);
		}

		@Override
		public String toString() {
			return "Input{" + "messageFrom='" + messageFrom + '\'' + ", triggeredBy='"
					+ triggeredBy + '\'' + ", messageHeaders=" + messageHeaders
					+ ", messageBody=" + messageBody + ", messageBodyFromFile='"
					+ messageBodyFromFile + '\'' + ", messageBodyFromFileAsBytes='"
					+ messageBodyFromFileAsBytes + '\'' + ", assertThat='" + assertThat
					+ '\'' + ", matchers=" + matchers + '}';
		}

	}

	public static class OutputMessage {

		public String sentTo;

		public Map<String, Object> headers = new LinkedHashMap<String, Object>();

		public Object body;

		public String bodyFromFile;

		public String bodyFromFileAsBytes;

		public String assertThat;

		public TestMatchers matchers = new TestMatchers();

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || getClass() != o.getClass()) {
				return false;
			}
			OutputMessage that = (OutputMessage) o;
			return Objects.equals(sentTo, that.sentTo)
					&& Objects.equals(headers, that.headers)
					&& Objects.equals(body, that.body)
					&& Objects.equals(bodyFromFile, that.bodyFromFile)
					&& Objects.equals(bodyFromFileAsBytes, that.bodyFromFileAsBytes)
					&& Objects.equals(assertThat, that.assertThat)
					&& Objects.equals(matchers, that.matchers);
		}

		@Override
		public int hashCode() {
			return Objects.hash(sentTo, headers, body, bodyFromFile, bodyFromFileAsBytes,
					assertThat, matchers);
		}

		@Override
		public String toString() {
			return "OutputMessage{" + "sentTo='" + sentTo + '\'' + ", headers=" + headers
					+ ", body=" + body + ", bodyFromFile='" + bodyFromFile + '\''
					+ ", bodyFromFileAsBytes='" + bodyFromFileAsBytes + '\''
					+ ", assertThat='" + assertThat + '\'' + ", matchers=" + matchers
					+ '}';
		}

	}

}
