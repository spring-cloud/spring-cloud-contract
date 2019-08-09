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

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.regex.Pattern;

/**
 * Represents a set of headers of a request / response or a message.
 *
 * @author Marcin Grzejszczak
 * @author Olga Maciaszek-Sharma
 * @author Tim Ysewyn
 * @since 1.0.0
 */
public class Headers {

	private static final BiFunction<String, Header, Object> CLIENT_SIDE = (s,
			header) -> ContractUtils.convertStubSideRecursively(header);

	private static final BiFunction<String, Header, Object> SERVER_SIDE = (s,
			header) -> ContractUtils.convertTestSideRecursively(header);

	private Set<Header> entries = new LinkedHashSet<>();

	public void header(Map<String, Object> singleHeader) {
		Iterator<Map.Entry<String, Object>> iterator = singleHeader.entrySet().iterator();
		if (iterator.hasNext()) {
			Map.Entry<String, Object> first = iterator.next();
			if (first != null) {
				entries.add(Header.build(first.getKey(), first.getValue()));
			}
		}
	}

	public void header(String headerKey, Object headerValue) {
		entries.add(Header.build(headerKey, headerValue));
	}

	public void executeForEachHeader(final Consumer<Header> consumer) {
		entries.forEach(consumer);
	}

	public void headers(Set<Header> headers) {
		entries.addAll(headers);
	}

	public void accept(String contentType) {
		header(accept(), matching(contentType));
	}

	public void contentType(String contentType) {
		header(HttpHeaders.CONTENT_TYPE, matching(contentType));
	}

	public void messagingContentType(String contentType) {
		header(MessagingHeaders.MESSAGING_CONTENT_TYPE, matching(contentType));
	}

	/**
	 * If for the consumer / producer you want to match exactly only the root of content
	 * type. I.e. {@code application/json;charset=UTF8} you care only about
	 * {@code application/json} then you should use this method
	 * @param value regex as String
	 * @return dsl property
	 */
	public DslProperty matching(String value) {
		return new DslProperty(value);
	}

	protected NotToEscapePattern notEscaped(Pattern pattern) {
		return new NotToEscapePattern(pattern);
	}

	public Map<String, Object> asMap(final BiFunction<String, Header, Object> consumer) {
		final Map<String, Object> map = new LinkedHashMap<>();
		entries.forEach(header -> map.put(header.getName(),
				consumer.apply(header.getName(), header)));
		return map;
	}

	/**
	 * Converts the headers into their stub side representations and returns as a map of
	 * String key =&gt; Object value.
	 * @return converted map
	 */
	public Map<String, Object> asStubSideMap() {
		return asMap(CLIENT_SIDE);
	}

	/**
	 * Converts the headers into their stub side representations and returns as a map of
	 * String key =&gt; Object value.
	 * @return converted map
	 */
	public Map<String, Object> asTestSideMap() {
		return asMap(SERVER_SIDE);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		Headers headers = (Headers) o;
		return Objects.equals(entries, headers.entries);
	}

	@Override
	public int hashCode() {
		return Objects.hash(entries);
	}

	@Override
	public String toString() {
		return "Headers{" + "\nentries=" + entries + '}';
	}

	public Set<Header> getEntries() {
		return entries;
	}

	public void setEntries(Set<Header> entries) {
		this.entries = entries;
	}

	public String messagingContentType() {
		return MessagingHeaders.MESSAGING_CONTENT_TYPE;
	}

	public String accept() {
		return HttpHeaders.ACCEPT;
	}

	public String acceptCharset() {
		return HttpHeaders.ACCEPT_CHARSET;
	}

	public String acceptEncoding() {
		return HttpHeaders.ACCEPT_ENCODING;
	}

	public String acceptLanguage() {
		return HttpHeaders.ACCEPT_LANGUAGE;
	}

	public String acceptRanges() {
		return HttpHeaders.ACCEPT_RANGES;
	}

	public String accessControlAllowCredentials() {
		return HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS;
	}

	public String accessControlAllowHeaders() {
		return HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS;
	}

	public String accessControlAllowMethods() {
		return HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS;
	}

	public String accessControlAllowOrigin() {
		return HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN;
	}

	public String accessControlExposeHeaders() {
		return HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS;
	}

	public String accessControlMaxAge() {
		return HttpHeaders.ACCESS_CONTROL_MAX_AGE;
	}

	public String accessControlRequestHeaders() {
		return HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS;
	}

	public String accessControlRequestMethod() {
		return HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD;
	}

	public String age() {
		return HttpHeaders.AGE;
	}

	public String allow() {
		return HttpHeaders.ALLOW;
	}

	public String authorization() {
		return HttpHeaders.AUTHORIZATION;
	}

	public String cacheControl() {
		return HttpHeaders.CACHE_CONTROL;
	}

	public String connection() {
		return HttpHeaders.CONNECTION;
	}

	public String contentEncoding() {
		return HttpHeaders.CONTENT_ENCODING;
	}

	public String contentDisposition() {
		return HttpHeaders.CONTENT_DISPOSITION;
	}

	public String contentLanguage() {
		return HttpHeaders.CONTENT_LANGUAGE;
	}

	public String contentLength() {
		return HttpHeaders.CONTENT_LENGTH;
	}

	public String contentLocation() {
		return HttpHeaders.CONTENT_LOCATION;
	}

	public String contentRange() {
		return HttpHeaders.CONTENT_RANGE;
	}

	public String contentType() {
		return HttpHeaders.CONTENT_TYPE;
	}

	public String cookie() {
		return HttpHeaders.COOKIE;
	}

	public String date() {
		return HttpHeaders.DATE;
	}

	public String etag() {
		return HttpHeaders.ETAG;
	}

	public String expect() {
		return HttpHeaders.EXPECT;
	}

	public String expires() {
		return HttpHeaders.EXPIRES;
	}

	public String from() {
		return HttpHeaders.FROM;
	}

	public String host() {
		return HttpHeaders.HOST;
	}

	public String ifMatch() {
		return HttpHeaders.IF_MATCH;
	}

	public String ifModifiedSince() {
		return HttpHeaders.IF_MODIFIED_SINCE;
	}

	public String ifNoneMatch() {
		return HttpHeaders.IF_NONE_MATCH;
	}

	public String ifRange() {
		return HttpHeaders.IF_RANGE;
	}

	public String ifUnmodifiedSince() {
		return HttpHeaders.IF_UNMODIFIED_SINCE;
	}

	public String lastModified() {
		return HttpHeaders.LAST_MODIFIED;
	}

	public String link() {
		return HttpHeaders.LINK;
	}

	public String location() {
		return HttpHeaders.LOCATION;
	}

	public String max_forwards() {
		return HttpHeaders.MAX_FORWARDS;
	}

	public String origin() {
		return HttpHeaders.ORIGIN;
	}

	public String pragma() {
		return HttpHeaders.PRAGMA;
	}

	public String proxyAuthenticate() {
		return HttpHeaders.PROXY_AUTHENTICATE;
	}

	public String proxyAuthorization() {
		return HttpHeaders.PROXY_AUTHORIZATION;
	}

	public String range() {
		return HttpHeaders.RANGE;
	}

	public String referer() {
		return HttpHeaders.REFERER;
	}

	public String retryAfter() {
		return HttpHeaders.RETRY_AFTER;
	}

	public String server() {
		return HttpHeaders.SERVER;
	}

	public String setCookie() {
		return HttpHeaders.SET_COOKIE;
	}

	public String setCookie2() {
		return HttpHeaders.SET_COOKIE_2;
	}

	public String te() {
		return HttpHeaders.TE;
	}

	public String trailer() {
		return HttpHeaders.TRAILER;
	}

	public String transferEncoding() {
		return HttpHeaders.TRANSFER_ENCODING;
	}

	public String upgrade() {
		return HttpHeaders.UPGRADE;
	}

	public String user_agent() {
		return HttpHeaders.USER_AGENT;
	}

	public String vary() {
		return HttpHeaders.VARY;
	}

	public String via() {
		return HttpHeaders.VIA;
	}

	public String warning() {
		return HttpHeaders.WARNING;
	}

	public String wwwAuthenticate() {
		return HttpHeaders.WWW_AUTHENTICATE;
	}

	public String allValue() {
		return MediaTypes.ALL_VALUE;
	}

	public String applicationAtomXml() {
		return MediaTypes.APPLICATION_ATOM_XML;
	}

	public String applicationFormUrlencoded() {
		return MediaTypes.APPLICATION_FORM_URLENCODED;
	}

	public String applicationJson() {
		return MediaTypes.APPLICATION_JSON;
	}

	public String applicationJsonUtf8() {
		return MediaTypes.APPLICATION_JSON_UTF8;
	}

	public String applicationOctetStream() {
		return MediaTypes.APPLICATION_OCTET_STREAM;
	}

	public String applicationPdf() {
		return MediaTypes.APPLICATION_PDF;
	}

	public String applicationXhtmlXml() {
		return MediaTypes.APPLICATION_XHTML_XML;
	}

	public String applicationXml() {
		return MediaTypes.APPLICATION_XML;
	}

	public String imageGif() {
		return MediaTypes.IMAGE_GIF;
	}

	public String imageJpeg() {
		return MediaTypes.IMAGE_JPEG;
	}

	public String imagePng() {
		return MediaTypes.IMAGE_PNG;
	}

	public String multipartFormData() {
		return MediaTypes.MULTIPART_FORM_DATA;
	}

	public String textHtml() {
		return MediaTypes.TEXT_HTML;
	}

	public String textMarkdown() {
		return MediaTypes.TEXT_MARKDOWN;
	}

	public String textPlain() {
		return MediaTypes.TEXT_PLAIN;
	}

	public String textXml() {
		return MediaTypes.TEXT_XML;
	}

}
