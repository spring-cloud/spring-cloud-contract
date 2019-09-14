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

import org.assertj.core.api.BDDAssertions;
import org.junit.jupiter.api.Test;

/**
 * @author Tim Ysewyn
 */
class HttpHeadersTests {

	@Test
	public void ACCEPT() {
		BDDAssertions.then(HttpHeaders.ACCEPT).isEqualTo("Accept");
		BDDAssertions.then(new HttpHeaders().accept()).isEqualTo(HttpHeaders.ACCEPT);
	}

	@Test
	public void ACCEPT_CHARSET() {
		BDDAssertions.then(HttpHeaders.ACCEPT_CHARSET).isEqualTo("Accept-Charset");
		BDDAssertions.then(new HttpHeaders().acceptCharset())
				.isEqualTo(HttpHeaders.ACCEPT_CHARSET);
	}

	@Test
	public void ACCEPT_ENCODING() {
		BDDAssertions.then(HttpHeaders.ACCEPT_ENCODING).isEqualTo("Accept-Encoding");
		BDDAssertions.then(new HttpHeaders().acceptEncoding())
				.isEqualTo(HttpHeaders.ACCEPT_ENCODING);
	}

	@Test
	public void ACCEPT_LANGUAGE() {
		BDDAssertions.then(HttpHeaders.ACCEPT_LANGUAGE).isEqualTo("Accept-Language");
		BDDAssertions.then(new HttpHeaders().acceptLanguage())
				.isEqualTo(HttpHeaders.ACCEPT_LANGUAGE);
	}

	@Test
	public void ACCEPT_RANGES() {
		BDDAssertions.then(HttpHeaders.ACCEPT_RANGES).isEqualTo("Accept-Ranges");
		BDDAssertions.then(new HttpHeaders().acceptRanges())
				.isEqualTo(HttpHeaders.ACCEPT_RANGES);
	}

	@Test
	public void ACCESS_CONTROL_ALLOW_CREDENTIALS() {
		BDDAssertions.then(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS)
				.isEqualTo("Access-Control-Allow-Credentials");
		BDDAssertions.then(new HttpHeaders().accessControlAllowCredentials())
				.isEqualTo(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS);
	}

	@Test
	public void ACCESS_CONTROL_ALLOW_HEADERS() {
		BDDAssertions.then(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS)
				.isEqualTo("Access-Control-Allow-Headers");
		BDDAssertions.then(new HttpHeaders().accessControlAllowHeaders())
				.isEqualTo(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS);
	}

	@Test
	public void ACCESS_CONTROL_ALLOW_METHODS() {
		BDDAssertions.then(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS)
				.isEqualTo("Access-Control-Allow-Methods");
		BDDAssertions.then(new HttpHeaders().accessControlAllowMethods())
				.isEqualTo(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS);
	}

	@Test
	public void ACCESS_CONTROL_ALLOW_ORIGIN() {
		BDDAssertions.then(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN)
				.isEqualTo("Access-Control-Allow-Origin");
		BDDAssertions.then(new HttpHeaders().accessControlAllowOrigin())
				.isEqualTo(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN);
	}

	@Test
	public void ACCESS_CONTROL_EXPOSE_HEADERS() {
		BDDAssertions.then(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS)
				.isEqualTo("Access-Control-Expose-Headers");
		BDDAssertions.then(new HttpHeaders().accessControlExposeHeaders())
				.isEqualTo(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS);
	}

	@Test
	public void ACCESS_CONTROL_MAX_AGE() {
		BDDAssertions.then(HttpHeaders.ACCESS_CONTROL_MAX_AGE)
				.isEqualTo("Access-Control-Max-Age");
		BDDAssertions.then(new HttpHeaders().accessControlMaxAge())
				.isEqualTo(HttpHeaders.ACCESS_CONTROL_MAX_AGE);
	}

	@Test
	public void ACCESS_CONTROL_REQUEST_HEADERS() {
		BDDAssertions.then(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS)
				.isEqualTo("Access-Control-Request-Headers");
		BDDAssertions.then(new HttpHeaders().accessControlRequestHeaders())
				.isEqualTo(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS);
	}

	@Test
	public void ACCESS_CONTROL_REQUEST_METHOD() {
		BDDAssertions.then(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD)
				.isEqualTo("Access-Control-Request-Method");
		BDDAssertions.then(new HttpHeaders().accessControlRequestMethod())
				.isEqualTo(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD);
	}

	@Test
	public void AGE() {
		BDDAssertions.then(HttpHeaders.AGE).isEqualTo("Age");
		BDDAssertions.then(new HttpHeaders().age()).isEqualTo(HttpHeaders.AGE);
	}

	@Test
	public void ALLOW() {
		BDDAssertions.then(HttpHeaders.ALLOW).isEqualTo("Allow");
		BDDAssertions.then(new HttpHeaders().allow()).isEqualTo(HttpHeaders.ALLOW);
	}

	@Test
	public void AUTHORIZATION() {
		BDDAssertions.then(HttpHeaders.AUTHORIZATION).isEqualTo("Authorization");
		BDDAssertions.then(new HttpHeaders().authorization())
				.isEqualTo(HttpHeaders.AUTHORIZATION);
	}

	@Test
	public void CACHE_CONTROL() {
		BDDAssertions.then(HttpHeaders.CACHE_CONTROL).isEqualTo("Cache-Control");
		BDDAssertions.then(new HttpHeaders().cacheControl())
				.isEqualTo(HttpHeaders.CACHE_CONTROL);
	}

	@Test
	public void CONNECTION() {
		BDDAssertions.then(HttpHeaders.CONNECTION).isEqualTo("Connection");
		BDDAssertions.then(new HttpHeaders().connection())
				.isEqualTo(HttpHeaders.CONNECTION);
	}

	@Test
	public void CONTENT_ENCODING() {
		BDDAssertions.then(HttpHeaders.CONTENT_ENCODING).isEqualTo("Content-Encoding");
		BDDAssertions.then(new HttpHeaders().contentEncoding())
				.isEqualTo(HttpHeaders.CONTENT_ENCODING);
	}

	@Test
	public void CONTENT_DISPOSITION() {
		BDDAssertions.then(HttpHeaders.CONTENT_DISPOSITION)
				.isEqualTo("Content-Disposition");
		BDDAssertions.then(new HttpHeaders().contentDisposition())
				.isEqualTo(HttpHeaders.CONTENT_DISPOSITION);
	}

	@Test
	public void CONTENT_LANGUAGE() {
		BDDAssertions.then(HttpHeaders.CONTENT_LANGUAGE).isEqualTo("Content-Language");
		BDDAssertions.then(new HttpHeaders().contentLanguage())
				.isEqualTo(HttpHeaders.CONTENT_LANGUAGE);
	}

	@Test
	public void CONTENT_LENGTH() {
		BDDAssertions.then(HttpHeaders.CONTENT_LENGTH).isEqualTo("Content-Length");
		BDDAssertions.then(new HttpHeaders().contentLength())
				.isEqualTo(HttpHeaders.CONTENT_LENGTH);
	}

	@Test
	public void CONTENT_LOCATION() {
		BDDAssertions.then(HttpHeaders.CONTENT_LOCATION).isEqualTo("Content-Location");
		BDDAssertions.then(new HttpHeaders().contentLocation())
				.isEqualTo(HttpHeaders.CONTENT_LOCATION);
	}

	@Test
	public void CONTENT_RANGE() {
		BDDAssertions.then(HttpHeaders.CONTENT_RANGE).isEqualTo("Content-Range");
		BDDAssertions.then(new HttpHeaders().contentRange())
				.isEqualTo(HttpHeaders.CONTENT_RANGE);
	}

	@Test
	public void CONTENT_TYPE() {
		BDDAssertions.then(HttpHeaders.CONTENT_TYPE).isEqualTo("Content-Type");
		BDDAssertions.then(new HttpHeaders().contentType())
				.isEqualTo(HttpHeaders.CONTENT_TYPE);
	}

	@Test
	public void COOKIE() {
		BDDAssertions.then(HttpHeaders.COOKIE).isEqualTo("Cookie");
		BDDAssertions.then(new HttpHeaders().cookie()).isEqualTo(HttpHeaders.COOKIE);
	}

	@Test
	public void DATE() {
		BDDAssertions.then(HttpHeaders.DATE).isEqualTo("Date");
		BDDAssertions.then(new HttpHeaders().date()).isEqualTo(HttpHeaders.DATE);
	}

	@Test
	public void ETAG() {
		BDDAssertions.then(HttpHeaders.ETAG).isEqualTo("ETag");
		BDDAssertions.then(new HttpHeaders().etag()).isEqualTo(HttpHeaders.ETAG);
	}

	@Test
	public void EXPECT() {
		BDDAssertions.then(HttpHeaders.EXPECT).isEqualTo("Expect");
		BDDAssertions.then(new HttpHeaders().expect()).isEqualTo(HttpHeaders.EXPECT);
	}

	@Test
	public void EXPIRES() {
		BDDAssertions.then(HttpHeaders.EXPIRES).isEqualTo("Expires");
		BDDAssertions.then(new HttpHeaders().expires()).isEqualTo(HttpHeaders.EXPIRES);
	}

	@Test
	public void FROM() {
		BDDAssertions.then(HttpHeaders.FROM).isEqualTo("From");
		BDDAssertions.then(new HttpHeaders().from()).isEqualTo(HttpHeaders.FROM);
	}

	@Test
	public void HOST() {
		BDDAssertions.then(HttpHeaders.HOST).isEqualTo("Host");
		BDDAssertions.then(new HttpHeaders().host()).isEqualTo(HttpHeaders.HOST);
	}

	@Test
	public void IF_MATCH() {
		BDDAssertions.then(HttpHeaders.IF_MATCH).isEqualTo("If-Match");
		BDDAssertions.then(new HttpHeaders().ifMatch()).isEqualTo(HttpHeaders.IF_MATCH);
	}

	@Test
	public void IF_MODIFIED_SINCE() {
		BDDAssertions.then(HttpHeaders.IF_MODIFIED_SINCE).isEqualTo("If-Modified-Since");
		BDDAssertions.then(new HttpHeaders().ifModifiedSince())
				.isEqualTo(HttpHeaders.IF_MODIFIED_SINCE);
	}

	@Test
	public void IF_NONE_MATCH() {
		BDDAssertions.then(HttpHeaders.IF_NONE_MATCH).isEqualTo("If-None-Match");
		BDDAssertions.then(new HttpHeaders().ifNoneMatch())
				.isEqualTo(HttpHeaders.IF_NONE_MATCH);
	}

	@Test
	public void IF_RANGE() {
		BDDAssertions.then(HttpHeaders.IF_RANGE).isEqualTo("If-Range");
		BDDAssertions.then(new HttpHeaders().ifRange()).isEqualTo(HttpHeaders.IF_RANGE);
	}

	@Test
	public void IF_UNMODIFIED_SINCE() {
		BDDAssertions.then(HttpHeaders.IF_UNMODIFIED_SINCE)
				.isEqualTo("If-Unmodified-Since");
		BDDAssertions.then(new HttpHeaders().ifUnmodifiedSince())
				.isEqualTo(HttpHeaders.IF_UNMODIFIED_SINCE);
	}

	@Test
	public void LAST_MODIFIED() {
		BDDAssertions.then(HttpHeaders.LAST_MODIFIED).isEqualTo("Last-Modified");
		BDDAssertions.then(new HttpHeaders().lastModified())
				.isEqualTo(HttpHeaders.LAST_MODIFIED);
	}

	@Test
	public void LINK() {
		BDDAssertions.then(HttpHeaders.LINK).isEqualTo("Link");
		BDDAssertions.then(new HttpHeaders().link()).isEqualTo(HttpHeaders.LINK);
	}

	@Test
	public void LOCATION() {
		BDDAssertions.then(HttpHeaders.LOCATION).isEqualTo("Location");
		BDDAssertions.then(new HttpHeaders().location()).isEqualTo(HttpHeaders.LOCATION);
	}

	@Test
	public void MAX_FORWARDS() {
		BDDAssertions.then(HttpHeaders.MAX_FORWARDS).isEqualTo("Max-Forwards");
		BDDAssertions.then(new HttpHeaders().max_forwards())
				.isEqualTo(HttpHeaders.MAX_FORWARDS);
	}

	@Test
	public void ORIGIN() {
		BDDAssertions.then(HttpHeaders.ORIGIN).isEqualTo("Origin");
		BDDAssertions.then(new HttpHeaders().origin()).isEqualTo(HttpHeaders.ORIGIN);
	}

	@Test
	public void PRAGMA() {
		BDDAssertions.then(HttpHeaders.PRAGMA).isEqualTo("Pragma");
		BDDAssertions.then(new HttpHeaders().pragma()).isEqualTo(HttpHeaders.PRAGMA);
	}

	@Test
	public void PROXY_AUTHENTICATE() {
		BDDAssertions.then(HttpHeaders.PROXY_AUTHENTICATE)
				.isEqualTo("Proxy-Authenticate");
		BDDAssertions.then(new HttpHeaders().proxyAuthenticate())
				.isEqualTo(HttpHeaders.PROXY_AUTHENTICATE);
	}

	@Test
	public void PROXY_AUTHORIZATION() {
		BDDAssertions.then(HttpHeaders.PROXY_AUTHORIZATION)
				.isEqualTo("Proxy-Authorization");
		BDDAssertions.then(new HttpHeaders().proxyAuthorization())
				.isEqualTo(HttpHeaders.PROXY_AUTHORIZATION);
	}

	@Test
	public void RANGE() {
		BDDAssertions.then(HttpHeaders.RANGE).isEqualTo("Range");
		BDDAssertions.then(new HttpHeaders().range()).isEqualTo(HttpHeaders.RANGE);
	}

	@Test
	public void REFERER() {
		BDDAssertions.then(HttpHeaders.REFERER).isEqualTo("Referer");
		BDDAssertions.then(new HttpHeaders().referer()).isEqualTo(HttpHeaders.REFERER);
	}

	@Test
	public void RETRY_AFTER() {
		BDDAssertions.then(HttpHeaders.RETRY_AFTER).isEqualTo("Retry-After");
		BDDAssertions.then(new HttpHeaders().retryAfter())
				.isEqualTo(HttpHeaders.RETRY_AFTER);
	}

	@Test
	public void SERVER() {
		BDDAssertions.then(HttpHeaders.SERVER).isEqualTo("Server");
		BDDAssertions.then(new HttpHeaders().server()).isEqualTo(HttpHeaders.SERVER);
	}

	@Test
	public void SET_COOKIE() {
		BDDAssertions.then(HttpHeaders.SET_COOKIE).isEqualTo("Set-Cookie");
		BDDAssertions.then(new HttpHeaders().setCookie())
				.isEqualTo(HttpHeaders.SET_COOKIE);
	}

	@Test
	public void SET_COOKIE_2() {
		BDDAssertions.then(HttpHeaders.SET_COOKIE_2).isEqualTo("Set-Cookie2");
		BDDAssertions.then(new HttpHeaders().setCookie2())
				.isEqualTo(HttpHeaders.SET_COOKIE_2);
	}

	@Test
	public void TE() {
		BDDAssertions.then(HttpHeaders.TE).isEqualTo("TE");
		BDDAssertions.then(new HttpHeaders().te()).isEqualTo(HttpHeaders.TE);
	}

	@Test
	public void TRAILER() {
		BDDAssertions.then(HttpHeaders.TRAILER).isEqualTo("Trailer");
		BDDAssertions.then(new HttpHeaders().trailer()).isEqualTo(HttpHeaders.TRAILER);
	}

	@Test
	public void TRANSFER_ENCODING() {
		BDDAssertions.then(HttpHeaders.TRANSFER_ENCODING).isEqualTo("Transfer-Encoding");
		BDDAssertions.then(new HttpHeaders().transferEncoding())
				.isEqualTo(HttpHeaders.TRANSFER_ENCODING);
	}

	@Test
	public void UPGRADE() {
		BDDAssertions.then(HttpHeaders.UPGRADE).isEqualTo("Upgrade");
		BDDAssertions.then(new HttpHeaders().upgrade()).isEqualTo(HttpHeaders.UPGRADE);
	}

	@Test
	public void USER_AGENT() {
		BDDAssertions.then(HttpHeaders.USER_AGENT).isEqualTo("User-Agent");
		BDDAssertions.then(new HttpHeaders().user_agent())
				.isEqualTo(HttpHeaders.USER_AGENT);
	}

	@Test
	public void VARY() {
		BDDAssertions.then(HttpHeaders.VARY).isEqualTo("Vary");
		BDDAssertions.then(new HttpHeaders().vary()).isEqualTo(HttpHeaders.VARY);
	}

	@Test
	public void VIA() {
		BDDAssertions.then(HttpHeaders.VIA).isEqualTo("Via");
		BDDAssertions.then(new HttpHeaders().via()).isEqualTo(HttpHeaders.VIA);
	}

	@Test
	public void WARNING() {
		BDDAssertions.then(HttpHeaders.WARNING).isEqualTo("Warning");
		BDDAssertions.then(new HttpHeaders().warning()).isEqualTo(HttpHeaders.WARNING);
	}

	@Test
	public void WWW_AUTHENTICATE() {
		BDDAssertions.then(HttpHeaders.WWW_AUTHENTICATE).isEqualTo("WWW-Authenticate");
		BDDAssertions.then(new HttpHeaders().wwwAuthenticate())
				.isEqualTo(HttpHeaders.WWW_AUTHENTICATE);
	}

}
