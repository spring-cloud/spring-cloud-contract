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

	private MediaTypes mediaTypes = new MediaTypes();

	private MessagingHeaders messagingHeaders = new MessagingHeaders();

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
		header(HttpHeaders.contentType(), matching(contentType));
	}

	public void messagingContentType(String contentType) {
		header(messagingHeaders.messagingContentType(), matching(contentType));
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
	 * String key => Object value.
	 * @return converted map
	 */
	public Map<String, Object> asStubSideMap() {
		return asMap(CLIENT_SIDE);
	}

	/**
	 * Converts the headers into their stub side representations and returns as a map of
	 * String key => Object value.
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

	public MediaTypes getMediaTypes() {
		return mediaTypes;
	}

	public void setMediaTypes(MediaTypes mediaTypes) {
		this.mediaTypes = mediaTypes;
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
		return HttpHeaders.accept();
	}

	public String acceptCharset() {
		return HttpHeaders.acceptCharset();
	}

	public String acceptEncoding() {
		return HttpHeaders.acceptEncoding();
	}

	public String acceptLanguage() {
		return HttpHeaders.acceptLanguage();
	}

	public String acceptRanges() {
		return HttpHeaders.acceptRanges();
	}

	public String accessControlAllowCredentials() {
		return HttpHeaders.accessControlAllowCredentials();
	}

	public String accessControlAllowHeaders() {
		return HttpHeaders.accessControlAllowHeaders();
	}

	public String accessControlAllowMethods() {
		return HttpHeaders.accessControlAllowMethods();
	}

	public String accessControlAllowOrigin() {
		return HttpHeaders.accessControlAllowOrigin();
	}

	public String accessControlExposeHeaders() {
		return HttpHeaders.accessControlExposeHeaders();
	}

	public String accessControlMaxAge() {
		return HttpHeaders.accessControlMaxAge();
	}

	public String accessControlRequestHeaders() {
		return HttpHeaders.accessControlRequestHeaders();
	}

	public String accessControlRequestMethod() {
		return HttpHeaders.accessControlRequestMethod();
	}

	public String age() {
		return HttpHeaders.age();
	}

	public String allow() {
		return HttpHeaders.allow();
	}

	public String authorization() {
		return HttpHeaders.authorization();
	}

	public String cacheControl() {
		return HttpHeaders.cacheControl();
	}

	public String connection() {
		return HttpHeaders.connection();
	}

	public String contentEncoding() {
		return HttpHeaders.contentEncoding();
	}

	public String contentDisposition() {
		return HttpHeaders.contentDisposition();
	}

	public String contentLanguage() {
		return HttpHeaders.contentLanguage();
	}

	public String contentLength() {
		return HttpHeaders.contentLength();
	}

	public String contentLocation() {
		return HttpHeaders.contentLocation();
	}

	public String contentRange() {
		return HttpHeaders.contentRange();
	}

	public String contentType() {
		return HttpHeaders.contentType();
	}

	public String cookie() {
		return HttpHeaders.cookie();
	}

	public String date() {
		return HttpHeaders.date();
	}

	public String etag() {
		return HttpHeaders.etag();
	}

	public String expect() {
		return HttpHeaders.expect();
	}

	public String expires() {
		return HttpHeaders.expires();
	}

	public String from() {
		return HttpHeaders.from();
	}

	public String host() {
		return HttpHeaders.host();
	}

	public String ifMatch() {
		return HttpHeaders.ifMatch();
	}

	public String ifModifiedSince() {
		return HttpHeaders.ifModifiedSince();
	}

	public String ifNoneMatch() {
		return HttpHeaders.ifNoneMatch();
	}

	public String ifRange() {
		return HttpHeaders.ifRange();
	}

	public String ifUnmodifiedSince() {
		return HttpHeaders.ifUnmodifiedSince();
	}

	public String lastModified() {
		return HttpHeaders.lastModified();
	}

	public String link() {
		return HttpHeaders.link();
	}

	public String location() {
		return HttpHeaders.location();
	}

	public String max_forwards() {
		return HttpHeaders.max_forwards();
	}

	public String origin() {
		return HttpHeaders.origin();
	}

	public String pragma() {
		return HttpHeaders.pragma();
	}

	public String proxyAuthenticate() {
		return HttpHeaders.proxyAuthenticate();
	}

	public String proxyAuthorization() {
		return HttpHeaders.proxyAuthorization();
	}

	public String range() {
		return HttpHeaders.range();
	}

	public String referer() {
		return HttpHeaders.referer();
	}

	public String retryAfter() {
		return HttpHeaders.retryAfter();
	}

	public String server() {
		return HttpHeaders.server();
	}

	public String setCookie() {
		return HttpHeaders.setCookie();
	}

	public String setCookie2() {
		return HttpHeaders.setCookie2();
	}

	public String te() {
		return HttpHeaders.te();
	}

	public String trailer() {
		return HttpHeaders.trailer();
	}

	public String transferEncoding() {
		return HttpHeaders.transferEncoding();
	}

	public String upgrade() {
		return HttpHeaders.upgrade();
	}

	public String user_agent() {
		return HttpHeaders.user_agent();
	}

	public String vary() {
		return HttpHeaders.vary();
	}

	public String via() {
		return HttpHeaders.via();
	}

	public String warning() {
		return HttpHeaders.warning();
	}

	public String wwwAuthenticate() {
		return HttpHeaders.wwwAuthenticate();
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
