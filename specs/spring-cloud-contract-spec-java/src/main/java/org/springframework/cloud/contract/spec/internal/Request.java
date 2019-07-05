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

package org.springframework.cloud.contract.spec.internal;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

import org.springframework.cloud.contract.spec.util.RegexpUtils;

/**
 * Represents the request side of the HTTP communication.
 *
 * @author Marcin Grzejszczak
 * @author Tim Ysewyn
 * @since 1.0.0
 */
public class Request extends Common implements RegexCreatingProperty<ClientDslProperty> {

	private ClientPatternValueDslProperty property = new ClientPatternValueDslProperty();

	private HttpMethods httpMethods = new HttpMethods();

	private DslProperty method;

	private Url url;

	private UrlPath urlPath;

	private Headers headers;

	private Cookies cookies;

	private Body body;

	private Multipart multipart;

	private BodyMatchers bodyMatchers;

	public Request() {
	}

	public Request(Request request) {
		this.method = request.getMethod();
		this.url = request.getUrl();
		this.urlPath = request.getUrlPath();
		this.headers = request.getHeaders();
		this.cookies = request.getCookies();
		this.body = request.getBody();
		this.multipart = request.getMultipart();
	}

	/**
	 * Name of the HTTP method.
	 * @param method HTTP method name
	 */
	public void method(String method) {
		this.method = toDslProperty(method);
	}

	/**
	 * Name of the HTTP method.
	 * @param httpMethod HTTP method name
	 */
	public void method(HttpMethods.HttpMethod httpMethod) {
		this.method = toDslProperty(httpMethod.toString());
	}

	/**
	 * Name of the HTTP method.
	 * @param method HTTP method name
	 */
	public void method(DslProperty method) {
		this.method = toDslProperty(method);
	}

	/**
	 * @param url URL to which the request will be sent
	 */
	public void url(Object url) {
		this.url = new Url(url);
	}

	/**
	 * @param url URL to which the request will be sent
	 */
	public void url(DslProperty url) {
		this.url = new Url(url);
	}

	/**
	 * @param path URL to which the request will be sent
	 */
	public void urlPath(String path) {
		this.urlPath = new UrlPath(path);
	}

	/**
	 * @param path URL to which the request will be sent
	 */
	public void urlPath(Object path) {
		this.urlPath = new UrlPath(path);
	}

	/**
	 * @param path URL to which the request will be sent
	 */
	public void urlPath(DslProperty path) {
		this.urlPath = new UrlPath(path);
	}

	/**
	 * Allows set an HTTP body.
	 * @param body body to set
	 */
	public void body(Map<String, Object> body) {
		this.body = new Body(convertObjectsToDslProperties(body));
	}

	/**
	 * Allows set an HTTP body.
	 * @param body body to set
	 */
	public void body(List body) {
		this.body = new Body(convertObjectsToDslProperties(body));
	}

	/**
	 * Allows set an HTTP body.
	 * @param matchingStrategy body to set
	 */
	public void body(MatchingStrategy matchingStrategy) {
		this.body = new Body(matchingStrategy);
	}

	/**
	 * Allows set an HTTP body.
	 * @param dslProperty body to set
	 */
	public void body(DslProperty dslProperty) {
		this.body = new Body(dslProperty);
	}

	/**
	 * Allows set an HTTP body.
	 * @param bodyAsValue body to set
	 */
	public void body(Object bodyAsValue) {
		this.body = new Body(bodyAsValue);
	}

	public Body getBody() {
		return body;
	}

	public void setBody(Body body) {
		this.body = body;
	}

	/**
	 * Allows to set multipart via the map notation.
	 * @param body multipart in a map notation
	 */
	public void multipart(Map<String, Object> body) {
		this.multipart = new Multipart(convertObjectsToDslProperties(body));
	}

	/**
	 * Allows to set multipart via lists.
	 * @param multipartAsList multipart in a list notation
	 */
	public void multipart(List multipartAsList) {
		this.multipart = Multipart.build(convertObjectsToDslProperties(multipartAsList));
	}

	/**
	 * Allows to set multipart value.
	 * @param dslProperty multipart
	 */
	public void multipart(DslProperty dslProperty) {
		this.multipart = new Multipart(dslProperty);
	}

	/**
	 * Allows to set multipart value.
	 * @param multipartAsValue multipart
	 */
	public void multipart(Object multipartAsValue) {
		this.multipart = Multipart.build(multipartAsValue);
	}

	/**
	 * Sets the equality check to the given query parameter.
	 * @param value value to check against
	 * @return matching strategy
	 */
	public MatchingStrategy equalTo(Object value) {
		return new MatchingStrategy(value, MatchingStrategy.Type.EQUAL_TO);
	}

	/**
	 * Sets the containing check to the given query parameter.
	 * @param value value to check against
	 * @return matching strategy
	 */
	public MatchingStrategy containing(Object value) {
		return new MatchingStrategy(value, MatchingStrategy.Type.CONTAINS);
	}

	/**
	 * Sets the matching check to the given query parameter.
	 * @param value value to check against
	 * @return matching strategy
	 */
	public MatchingStrategy matching(Object value) {
		return new MatchingStrategy(value, MatchingStrategy.Type.MATCHING);
	}

	/**
	 * Sets the not matching check to the given query parameter.
	 * @param value value to check against
	 * @return matching strategy
	 */
	public MatchingStrategy notMatching(Object value) {
		return new MatchingStrategy(value, MatchingStrategy.Type.NOT_MATCHING);
	}

	/**
	 * Sets the XML equality check to the body.
	 * @param value value to check against
	 * @return matching strategy
	 */
	public MatchingStrategy equalToXml(Object value) {
		return new MatchingStrategy(value, MatchingStrategy.Type.EQUAL_TO_XML);
	}

	/**
	 * Sets the JSON equality check to the body.
	 * @param value value to check against
	 * @return matching strategy
	 */
	public MatchingStrategy equalToJson(Object value) {
		return new MatchingStrategy(value, MatchingStrategy.Type.EQUAL_TO_JSON);
	}

	/**
	 * Sets absence check to the given query parameter.
	 * @return matching strategy
	 */
	public MatchingStrategy absent() {
		return new MatchingStrategy(true, MatchingStrategy.Type.ABSENT);
	}

	@Override
	public void assertThatSidesMatch(Object stubSide, Object testSide) {
		if (testSide instanceof OptionalProperty) {
			throw new IllegalStateException(
					"Optional can be used only for the stub side of the request!");
		}
		super.assertThatSidesMatch(stubSide, testSide);
	}

	/**
	 * Allows to set a dynamic value for the given element.
	 * @param client client value
	 * @return dsl property
	 */
	public DslProperty value(ClientDslProperty client) {
		Object concreteValue = client.getServerValue();
		Object dynamicValue = client.getClientValue();
		if (dynamicValue instanceof RegexProperty && client.isSingleValue()) {
			return ((RegexProperty) dynamicValue).dynamicClientEscapedConcreteProducer();
		}
		else if (concreteValue instanceof RegexProperty && !client.isSingleValue()) {
			concreteValue = dynamicValue;
		}

		return new DslProperty(dynamicValue, concreteValue);
	}

	/**
	 * Allows to set a dynamic value for the given regular expression element.
	 * @param property property to set
	 * @return dsl property
	 */
	public DslProperty $(RegexProperty property) {
		return value(property);
	}

	/**
	 * Allows to set a dynamic value for the given regular expression element.
	 * @param property property to set
	 * @return dsl property
	 */
	public DslProperty value(RegexProperty property) {
		return value(client(property));
	}

	/**
	 * Allows to set a dynamic value for the given element.
	 * @param client property to set
	 * @return dsl property
	 */
	public DslProperty $(ClientDslProperty client) {
		return value(client);
	}

	/**
	 * Allows to set a dynamic value for the Pattern element.
	 * @param client property to set
	 * @return dsl property
	 */
	public DslProperty value(Pattern client) {
		return value(new RegexProperty(client));
	}

	/**
	 * Allows to set a dynamic value for the given Pattern element.
	 * @param client property to set
	 * @return dsl property
	 */
	public DslProperty $(Pattern client) {
		return value(client);
	}

	@Override
	public RegexProperty regexProperty(Object object) {
		return new RegexProperty(object).dynamicClientConcreteProducer();
	}

	/**
	 * Allows to set a dynamic value for client and server side.
	 * @param client client value
	 * @param server server value
	 * @return dsl property
	 */
	@Override
	public DslProperty value(ClientDslProperty client, ServerDslProperty server) {
		if (server.getClientValue() instanceof RegexProperty) {
			throw new IllegalStateException(
					"You can't have a regular expression for the request on the server side");
		}
		return super.value(client, server);
	}

	/**
	 * Allows to set a dynamic value for client and server side.
	 * @param client client value
	 * @param server server value
	 * @return dsl property
	 */
	@Override
	public DslProperty value(ServerDslProperty server, ClientDslProperty client) {
		if (server.getClientValue() instanceof RegexProperty) {
			throw new IllegalStateException(
					"You can't have a regular expression for the request on the server side");
		}

		return super.value(server, client);
	}

	public ClientPatternValueDslProperty getProperty() {
		return property;
	}

	public void setProperty(ClientPatternValueDslProperty property) {
		this.property = property;
	}

	public HttpMethods getHttpMethods() {
		return httpMethods;
	}

	public void setHttpMethods(HttpMethods httpMethods) {
		this.httpMethods = httpMethods;
	}

	public DslProperty getMethod() {
		return method;
	}

	public void setMethod(DslProperty method) {
		this.method = method;
	}

	public Url getUrl() {
		return url;
	}

	public void setUrl(Url url) {
		this.url = url;
	}

	public UrlPath getUrlPath() {
		return urlPath;
	}

	public void setUrlPath(UrlPath urlPath) {
		this.urlPath = urlPath;
	}

	public Headers getHeaders() {
		return headers;
	}

	public void setHeaders(Headers headers) {
		this.headers = headers;
	}

	public Cookies getCookies() {
		return cookies;
	}

	public void setCookies(Cookies cookies) {
		this.cookies = cookies;
	}

	public Multipart getMultipart() {
		return multipart;
	}

	public void setMultipart(Multipart multipart) {
		this.multipart = multipart;
	}

	public BodyMatchers getBodyMatchers() {
		return bodyMatchers;
	}

	public void setBodyMatchers(BodyMatchers bodyMatchers) {
		this.bodyMatchers = bodyMatchers;
	}

	@Override
	public ClientDslProperty anyAlphaUnicode() {
		return property.anyAlphaUnicode();
	}

	@Override
	public ClientDslProperty anyAlphaNumeric() {
		return property.anyAlphaNumeric();
	}

	@Override
	public ClientDslProperty anyNumber() {
		return property.anyNumber();
	}

	@Override
	public ClientDslProperty anyInteger() {
		return property.anyInteger();
	}

	@Override
	public ClientDslProperty anyPositiveInt() {
		return property.anyPositiveInt();
	}

	@Override
	public ClientDslProperty anyDouble() {
		return property.anyDouble();
	}

	@Override
	public ClientDslProperty anyHex() {
		return property.anyHex();
	}

	@Override
	public ClientDslProperty aBoolean() {
		return property.aBoolean();
	}

	@Override
	public ClientDslProperty anyIpAddress() {
		return property.anyIpAddress();
	}

	@Override
	public ClientDslProperty anyHostname() {
		return property.anyHostname();
	}

	@Override
	public ClientDslProperty anyEmail() {
		return property.anyEmail();
	}

	@Override
	public ClientDslProperty anyUrl() {
		return property.anyUrl();
	}

	@Override
	public ClientDslProperty anyHttpsUrl() {
		return property.anyHttpsUrl();
	}

	@Override
	public ClientDslProperty anyUuid() {
		return property.anyUuid();
	}

	@Override
	public ClientDslProperty anyDate() {
		return property.anyDate();
	}

	@Override
	public ClientDslProperty anyDateTime() {
		return property.anyDateTime();
	}

	@Override
	public ClientDslProperty anyTime() {
		return property.anyTime();
	}

	@Override
	public ClientDslProperty anyIso8601WithOffset() {
		return property.anyIso8601WithOffset();
	}

	@Override
	public ClientDslProperty anyNonBlankString() {
		return property.anyNonBlankString();
	}

	@Override
	public ClientDslProperty anyNonEmptyString() {
		return property.anyNonEmptyString();
	}

	@Override
	public ClientDslProperty anyOf(String... values) {
		return property.anyOf(values);
	}

	public HttpMethods.HttpMethod GET() {
		return httpMethods.GET();
	}

	public HttpMethods.HttpMethod HEAD() {
		return httpMethods.HEAD();
	}

	public HttpMethods.HttpMethod POST() {
		return httpMethods.POST();
	}

	public HttpMethods.HttpMethod PUT() {
		return httpMethods.PUT();
	}

	public HttpMethods.HttpMethod PATCH() {
		return httpMethods.PATCH();
	}

	public HttpMethods.HttpMethod DELETE() {
		return httpMethods.DELETE();
	}

	public HttpMethods.HttpMethod OPTIONS() {
		return httpMethods.OPTIONS();
	}

	public HttpMethods.HttpMethod TRACE() {
		return httpMethods.TRACE();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		Request request = (Request) o;
		return Objects.equals(method, request.method) && Objects.equals(url, request.url)
				&& Objects.equals(urlPath, request.urlPath)
				&& Objects.equals(headers, request.headers)
				&& Objects.equals(cookies, request.cookies)
				&& Objects.equals(body, request.body)
				&& Objects.equals(multipart, request.multipart)
				&& Objects.equals(bodyMatchers, request.bodyMatchers);
	}

	@Override
	public int hashCode() {
		return Objects.hash(method, url, urlPath, headers, cookies, body, multipart,
				bodyMatchers);
	}

	@Override
	public String toString() {
		return "Request{" + "method=" + method + ", url=" + url + ", urlPath=" + urlPath
				+ ", headers=" + headers + ", cookies=" + cookies + ", body=" + body
				+ ", multipart=" + multipart + ", bodyMatchers=" + bodyMatchers + '}';
	}

	static class RequestHeaders extends Headers {

		private final Common common = new Common();

		@Override
		public DslProperty matching(String value) {
			return this.common.$(this.common.c(this.common
					.regex(RegexpUtils.escapeSpecialRegexWithSingleEscape(value) + ".*")),
					this.common.p(value));
		}

	}

	static class RequestCookies extends Cookies {

		private final Common common = new Common();

		@Override
		public DslProperty matching(String value) {
			return this.common.$(this.common.c(this.common
					.regex(RegexpUtils.escapeSpecialRegexWithSingleEscape(value) + ".*")),
					this.common.p(value));
		}

	}

	private class ServerRequest extends Request {

		ServerRequest(Request enclosing, Request request) {
			super(request);
		}

	}

	private class ClientRequest extends Request {

		ClientRequest(Request enclosing, Request request) {
			super(request);
		}

	}

	private class ClientPatternValueDslProperty
			extends PatternValueDslProperty<ClientDslProperty> {

		@Override
		protected ClientDslProperty createProperty(Pattern pattern,
				Object generatedValue) {
			return new ClientDslProperty(pattern, generatedValue);
		}

	}

}
