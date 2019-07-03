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
 * Represents a set of headers of a request / response or a message
 *
 * @since 1.0.0
 */
public class Headers {

	private MediaTypes mediaTypes = new MediaTypes();

	private HttpHeaders httpHeaders = new HttpHeaders();

	private MessagingHeaders messagingHeaders = new MessagingHeaders();

	private Set<Header> entries = new LinkedHashSet<>();

	private static final BiFunction<String, Header, String> CLIENT_SIDE = (s,
			header) -> header.getClientValue().toString();

	private static final BiFunction<String, Header, String> SERVER_SIDE = (s,
			header) -> header.getServerValue().toString();

	public void header(Map<String, Object> singleHeader) {
		Iterator<Map.Entry<String, Object>> iterator = singleHeader.entrySet().iterator();
		if (iterator.hasNext()) {
			Map.Entry<String, Object> first = iterator.next();
			if (first != null) {
				entries.add(new Header(first.getKey(), first.getValue()));
			}
		}
	}

	public void header(String headerKey, Object headerValue) {
		entries.add(new Header(headerKey, headerValue));
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
		header(httpHeaders.contentType(), matching(contentType));
	}

	public void messagingContentType(String contentType) {
		header(messagingHeaders.messagingContentType(), matching(contentType));
	}

	/**
	 * If for the consumer / producer you want to match exactly only the root of content
	 * type. I.e. {@code application/json;charset=UTF8} you care only about
	 * {@code application/json} then you should use this method
	 */
	public DslProperty matching(String value) {
		return new DslProperty(value);
	}

	protected NotToEscapePattern notEscaped(Pattern pattern) {
		return new NotToEscapePattern(pattern);
	}

	public Map<String, Object> asMap(final BiFunction<String, Header, String> consumer) {
		final Map<String, Object> map = new LinkedHashMap<String, Object>();
		entries.forEach(header -> map.put(header.getName(),
				consumer.apply(header.getName(), header)));
		return map;
	}

	/**
	 * Converts the headers into their stub side representations and returns as a map of
	 * String key => Object value.
	 */
	public Map<String, Object> asStubSideMap() {
		return asMap(CLIENT_SIDE);
	}

	/**
	 * Converts the headers into their stub side representations and returns as a map of
	 * String key => Object value.
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
		return "Headers{" + "entries=" + entries + '}';
	}

	public MediaTypes getMediaTypes() {
		return mediaTypes;
	}

	public void setMediaTypes(MediaTypes mediaTypes) {
		this.mediaTypes = mediaTypes;
	}

	public HttpHeaders getHttpHeaders() {
		return httpHeaders;
	}

	public void setHttpHeaders(HttpHeaders httpHeaders) {
		this.httpHeaders = httpHeaders;
	}

	public MessagingHeaders getMessagingHeaders() {
		return messagingHeaders;
	}

	public void setMessagingHeaders(MessagingHeaders messagingHeaders) {
		this.messagingHeaders = messagingHeaders;
	}

	public Set<Header> getEntries() {
		return entries;
	}

	public void setEntries(Set<Header> entries) {
		this.entries = entries;
	}

	public String messagingContentType() {
		return messagingHeaders.messagingContentType();
	}

	public String accept() {
		return httpHeaders.accept();
	}

	public String acceptCharset() {
		return httpHeaders.acceptCharset();
	}

	public String acceptEncoding() {
		return httpHeaders.acceptEncoding();
	}

	public String acceptLanguage() {
		return httpHeaders.acceptLanguage();
	}

	public String acceptRanges() {
		return httpHeaders.acceptRanges();
	}

	public String accessControlAllowCredentials() {
		return httpHeaders.accessControlAllowCredentials();
	}

	public String accessControlAllowHeaders() {
		return httpHeaders.accessControlAllowHeaders();
	}

	public String accessControlAllowMethods() {
		return httpHeaders.accessControlAllowMethods();
	}

	public String accessControlAllowOrigin() {
		return httpHeaders.accessControlAllowOrigin();
	}

	public String accessControlExposeHeaders() {
		return httpHeaders.accessControlExposeHeaders();
	}

	public String accessControlMaxAge() {
		return httpHeaders.accessControlMaxAge();
	}

	public String accessControlRequestHeaders() {
		return httpHeaders.accessControlRequestHeaders();
	}

	public String accessControlRequestMethod() {
		return httpHeaders.accessControlRequestMethod();
	}

	public String age() {
		return httpHeaders.age();
	}

	public String allow() {
		return httpHeaders.allow();
	}

	public String authorization() {
		return httpHeaders.authorization();
	}

	public String cacheControl() {
		return httpHeaders.cacheControl();
	}

	public String connection() {
		return httpHeaders.connection();
	}

	public String contentEncoding() {
		return httpHeaders.contentEncoding();
	}

	public String contentDisposition() {
		return httpHeaders.contentDisposition();
	}

	public String contentLanguage() {
		return httpHeaders.contentLanguage();
	}

	public String contentLength() {
		return httpHeaders.contentLength();
	}

	public String contentLocation() {
		return httpHeaders.contentLocation();
	}

	public String contentRange() {
		return httpHeaders.contentRange();
	}

	public String contentType() {
		return httpHeaders.contentType();
	}

	public String cookie() {
		return httpHeaders.cookie();
	}

	public String date() {
		return httpHeaders.date();
	}

	public String etag() {
		return httpHeaders.etag();
	}

	public String expect() {
		return httpHeaders.expect();
	}

	public String expires() {
		return httpHeaders.expires();
	}

	public String from() {
		return httpHeaders.from();
	}

	public String host() {
		return httpHeaders.host();
	}

	public String ifMatch() {
		return httpHeaders.ifMatch();
	}

	public String ifModifiedSince() {
		return httpHeaders.ifModifiedSince();
	}

	public String ifNoneMatch() {
		return httpHeaders.ifNoneMatch();
	}

	public String ifRange() {
		return httpHeaders.ifRange();
	}

	public String ifUnmodifiedSince() {
		return httpHeaders.ifUnmodifiedSince();
	}

	public String lastModified() {
		return httpHeaders.lastModified();
	}

	public String link() {
		return httpHeaders.link();
	}

	public String location() {
		return httpHeaders.location();
	}

	public String max_forwards() {
		return httpHeaders.max_forwards();
	}

	public String origin() {
		return httpHeaders.origin();
	}

	public String pragma() {
		return httpHeaders.pragma();
	}

	public String proxyAuthenticate() {
		return httpHeaders.proxyAuthenticate();
	}

	public String proxyAuthorization() {
		return httpHeaders.proxyAuthorization();
	}

	public String range() {
		return httpHeaders.range();
	}

	public String referer() {
		return httpHeaders.referer();
	}

	public String retryAfter() {
		return httpHeaders.retryAfter();
	}

	public String server() {
		return httpHeaders.server();
	}

	public String setCookie() {
		return httpHeaders.setCookie();
	}

	public String setCookie2() {
		return httpHeaders.setCookie2();
	}

	public String te() {
		return httpHeaders.te();
	}

	public String trailer() {
		return httpHeaders.trailer();
	}

	public String transferEncoding() {
		return httpHeaders.transferEncoding();
	}

	public String upgrade() {
		return httpHeaders.upgrade();
	}

	public String user_agent() {
		return httpHeaders.user_agent();
	}

	public String vary() {
		return httpHeaders.vary();
	}

	public String via() {
		return httpHeaders.via();
	}

	public String warning() {
		return httpHeaders.warning();
	}

	public String wwwAuthenticate() {
		return httpHeaders.wwwAuthenticate();
	}

	public String allValue() {
		return mediaTypes.allValue();
	}

	public String applicationAtomXml() {
		return mediaTypes.applicationAtomXml();
	}

	public String applicationFormUrlencoded() {
		return mediaTypes.applicationFormUrlencoded();
	}

	public String applicationJson() {
		return mediaTypes.applicationJson();
	}

	public String applicationJsonUtf8() {
		return mediaTypes.applicationJsonUtf8();
	}

	public String applicationOctetStream() {
		return mediaTypes.applicationOctetStream();
	}

	public String applicationPdf() {
		return mediaTypes.applicationPdf();
	}

	public String applicationXhtmlXml() {
		return mediaTypes.applicationXhtmlXml();
	}

	public String applicationXml() {
		return mediaTypes.applicationXml();
	}

	public String imageGif() {
		return mediaTypes.imageGif();
	}

	public String imageJpeg() {
		return mediaTypes.imageJpeg();
	}

	public String imagePng() {
		return mediaTypes.imagePng();
	}

	public String multipartFormData() {
		return mediaTypes.multipartFormData();
	}

	public String textHtml() {
		return mediaTypes.textHtml();
	}

	public String textMarkdown() {
		return mediaTypes.textMarkdown();
	}

	public String textPlain() {
		return mediaTypes.textPlain();
	}

	public String textXml() {
		return mediaTypes.textXml();
	}

}
