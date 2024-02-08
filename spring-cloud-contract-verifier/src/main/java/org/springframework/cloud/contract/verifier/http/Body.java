/*
 * Copyright 2012-2020 the original author or authors.
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

package org.springframework.cloud.contract.verifier.http;

import java.nio.charset.Charset;

/**
 * Abstraction over an HTTP body.
 *
 * Warning! This API is experimental and can change in time.
 *
 * @author Marcin Grzejszczak
 * @since 3.0.0
 */
public class Body {

	final Object body;

	Body(Object body) {
		this.body = body;
	}

	/**
	 * @return body as byte array
	 */
	public byte[] asByteArray() {
		if (body instanceof byte[]) {
			return (byte[]) this.body;
		}
		return body.toString().getBytes();
	}

	/**
	 * @return body as string
	 */
	public String asString() {
		return asString(Charset.defaultCharset());
	}

	/**
	 * @param charset to encode the body
	 * @return body as string
	 */
	public String asString(Charset charset) {
		if (body instanceof String) {
			return (String) body;
		}
		else if (body instanceof byte[]) {
			return new String((byte[]) body, charset);
		}
		return body.toString();
	}

}
