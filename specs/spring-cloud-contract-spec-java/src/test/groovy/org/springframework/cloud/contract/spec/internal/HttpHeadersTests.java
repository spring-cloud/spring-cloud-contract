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

package org.springframework.cloud.contract.spec.internal;

import org.assertj.core.api.BDDAssertions;
import org.junit.jupiter.api.Test;

/**
 * @author Tim Ysewyn
 */
class HttpHeadersTests {

	@Test
	public void ACCEPT() {
		BDDAssertions.then(HttpHeaders.ACCEPT).isEqualTo("Accept");
	}

	@Test
	public void ACCEPT_CHARSET() {
		BDDAssertions.then(HttpHeaders.ACCEPT_CHARSET).isEqualTo("Accept-Charset");
	}

	@Test
	public void ACCEPT_ENCODING() {
		BDDAssertions.then(HttpHeaders.ACCEPT_ENCODING).isEqualTo("Accept-Encoding");
	}

	@Test
	public void ACCEPT_LANGUAGE() {
		BDDAssertions.then(HttpHeaders.ACCEPT_LANGUAGE).isEqualTo("Accept-Language");
	}

	@Test
	public void ACCEPT_RANGES() {
		BDDAssertions.then(HttpHeaders.ACCEPT_RANGES).isEqualTo("Accept-Ranges");
	}

	@Test
	public void ACCESS_CONTROL_ALLOW_CREDENTIALS() {
		BDDAssertions.then(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS).isEqualTo("Access-Control-Allow-Credentials");
	}

	@Test
	public void ACCESS_CONTROL_ALLOW_HEADERS() {
		BDDAssertions.then(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS).isEqualTo("Access-Control-Allow-Headers");
	}

	@Test
	public void ACCESS_CONTROL_ALLOW_METHODS() {
		BDDAssertions.then(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS).isEqualTo("Access-Control-Allow-Methods");
	}

	@Test
	public void ACCESS_CONTROL_ALLOW_ORIGIN() {
		BDDAssertions.then(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN).isEqualTo("Access-Control-Allow-Origin");
	}

	@Test
	public void ACCESS_CONTROL_EXPOSE_HEADERS() {
		BDDAssertions.then(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS).isEqualTo("Access-Control-Expose-Headers");
	}

	@Test
	public void ACCESS_CONTROL_MAX_AGE() {
		BDDAssertions.then(HttpHeaders.ACCESS_CONTROL_MAX_AGE).isEqualTo("Access-Control-Max-Age");
	}

	@Test
	public void ACCESS_CONTROL_REQUEST_HEADERS() {
		BDDAssertions.then(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS).isEqualTo("Access-Control-Request-Headers");
	}

	@Test
	public void ACCESS_CONTROL_REQUEST_METHOD() {
		BDDAssertions.then(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD).isEqualTo("Access-Control-Request-Method");
	}

	@Test
	public void AGE() {
		BDDAssertions.then(HttpHeaders.AGE).isEqualTo("Age");
	}

	@Test
	public void ALLOW() {
		BDDAssertions.then(HttpHeaders.ALLOW).isEqualTo("Allow");
	}

	@Test
	public void AUTHORIZATION() {
		BDDAssertions.then(HttpHeaders.AUTHORIZATION).isEqualTo("Authorization");
	}

	@Test
	public void CACHE_CONTROL() {
		BDDAssertions.then(HttpHeaders.CACHE_CONTROL).isEqualTo("Cache-Control");
	}

	@Test
	public void CONNECTION() {
		BDDAssertions.then(HttpHeaders.CONNECTION).isEqualTo("Connection");
	}

	@Test
	public void CONTENT_ENCODING() {
		BDDAssertions.then(HttpHeaders.CONTENT_ENCODING).isEqualTo("Content-Encoding");
	}

	@Test
	public void CONTENT_DISPOSITION() {
		BDDAssertions.then(HttpHeaders.CONTENT_DISPOSITION).isEqualTo("Content-Disposition");
	}

	@Test
	public void CONTENT_LANGUAGE() {
		BDDAssertions.then(HttpHeaders.CONTENT_LANGUAGE).isEqualTo("Content-Language");
	}

	@Test
	public void CONTENT_LENGTH() {
		BDDAssertions.then(HttpHeaders.CONTENT_LENGTH).isEqualTo("Content-Length");
	}

	@Test
	public void CONTENT_LOCATION() {
		BDDAssertions.then(HttpHeaders.CONTENT_LOCATION).isEqualTo("Content-Location");
	}

	@Test
	public void CONTENT_RANGE() {
		BDDAssertions.then(HttpHeaders.CONTENT_RANGE).isEqualTo("Content-Range");
	}

	@Test
	public void CONTENT_TYPE() {
		BDDAssertions.then(HttpHeaders.CONTENT_TYPE).isEqualTo("Content-Type");
	}

	@Test
	public void COOKIE() {
		BDDAssertions.then(HttpHeaders.COOKIE).isEqualTo("Cookie");
	}

	@Test
	public void DATE() {
		BDDAssertions.then(HttpHeaders.DATE).isEqualTo("Date");
	}

	@Test
	public void ETAG() {
		BDDAssertions.then(HttpHeaders.ETAG).isEqualTo("ETag");
	}

	@Test
	public void EXPECT() {
		BDDAssertions.then(HttpHeaders.EXPECT).isEqualTo("Expect");
	}

	@Test
	public void EXPIRES() {
		BDDAssertions.then(HttpHeaders.EXPIRES).isEqualTo("Expires");
	}

	@Test
	public void FROM() {
		BDDAssertions.then(HttpHeaders.FROM).isEqualTo("From");
	}

	@Test
	public void HOST() {
		BDDAssertions.then(HttpHeaders.HOST).isEqualTo("Host");
	}

	@Test
	public void IF_MATCH() {
		BDDAssertions.then(HttpHeaders.IF_MATCH).isEqualTo("If-Match");
	}

	@Test
	public void IF_MODIFIED_SINCE() {
		BDDAssertions.then(HttpHeaders.IF_MODIFIED_SINCE).isEqualTo("If-Modified-Since");
	}

	@Test
	public void IF_NONE_MATCH() {
		BDDAssertions.then(HttpHeaders.IF_NONE_MATCH).isEqualTo("If-None-Match");
	}

	@Test
	public void IF_RANGE() {
		BDDAssertions.then(HttpHeaders.IF_RANGE).isEqualTo("If-Range");
	}

	@Test
	public void IF_UNMODIFIED_SINCE() {
		BDDAssertions.then(HttpHeaders.IF_UNMODIFIED_SINCE).isEqualTo("If-Unmodified-Since");
	}

	@Test
	public void LAST_MODIFIED() {
		BDDAssertions.then(HttpHeaders.LAST_MODIFIED).isEqualTo("Last-Modified");
	}

	@Test
	public void LINK() {
		BDDAssertions.then(HttpHeaders.LINK).isEqualTo("Link");
	}

	@Test
	public void LOCATION() {
		BDDAssertions.then(HttpHeaders.LOCATION).isEqualTo("Location");
	}

	@Test
	public void MAX_FORWARDS() {
		BDDAssertions.then(HttpHeaders.MAX_FORWARDS).isEqualTo("Max-Forwards");
	}

	@Test
	public void ORIGIN() {
		BDDAssertions.then(HttpHeaders.ORIGIN).isEqualTo("Origin");
	}

	@Test
	public void PRAGMA() {
		BDDAssertions.then(HttpHeaders.PRAGMA).isEqualTo("Pragma");
	}

	@Test
	public void PROXY_AUTHENTICATE() {
		BDDAssertions.then(HttpHeaders.PROXY_AUTHENTICATE).isEqualTo("Proxy-Authenticate");
	}

	@Test
	public void PROXY_AUTHORIZATION() {
		BDDAssertions.then(HttpHeaders.PROXY_AUTHORIZATION).isEqualTo("Proxy-Authorization");
	}

	@Test
	public void RANGE() {
		BDDAssertions.then(HttpHeaders.RANGE).isEqualTo("Range");
	}

	@Test
	public void REFERER() {
		BDDAssertions.then(HttpHeaders.REFERER).isEqualTo("Referer");
	}

	@Test
	public void RETRY_AFTER() {
		BDDAssertions.then(HttpHeaders.RETRY_AFTER).isEqualTo("Retry-After");
	}

	@Test
	public void SERVER() {
		BDDAssertions.then(HttpHeaders.SERVER).isEqualTo("Server");
	}

	@Test
	public void SET_COOKIE() {
		BDDAssertions.then(HttpHeaders.SET_COOKIE).isEqualTo("Set-Cookie");
	}

	@Test
	public void SET_COOKIE_2() {
		BDDAssertions.then(HttpHeaders.SET_COOKIE_2).isEqualTo("Set-Cookie2");
	}

	@Test
	public void TE() {
		BDDAssertions.then(HttpHeaders.TE).isEqualTo("TE");
	}

	@Test
	public void TRAILER() {
		BDDAssertions.then(HttpHeaders.TRAILER).isEqualTo("Trailer");
	}

	@Test
	public void TRANSFER_ENCODING() {
		BDDAssertions.then(HttpHeaders.TRANSFER_ENCODING).isEqualTo("Transfer-Encoding");
	}

	@Test
	public void UPGRADE() {
		BDDAssertions.then(HttpHeaders.UPGRADE).isEqualTo("Upgrade");
	}

	@Test
	public void USER_AGENT() {
		BDDAssertions.then(HttpHeaders.USER_AGENT).isEqualTo("User-Agent");
	}

	@Test
	public void VARY() {
		BDDAssertions.then(HttpHeaders.VARY).isEqualTo("Vary");
	}

	@Test
	public void VIA() {
		BDDAssertions.then(HttpHeaders.VIA).isEqualTo("Via");
	}

	@Test
	public void WARNING() {
		BDDAssertions.then(HttpHeaders.WARNING).isEqualTo("Warning");
	}

	@Test
	public void WWW_AUTHENTICATE() {
		BDDAssertions.then(HttpHeaders.WWW_AUTHENTICATE).isEqualTo("WWW-Authenticate");
	}

}
