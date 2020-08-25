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

package org.springframework.cloud.contract.verifier.util;

/**
 * Represents content type. Used to pick the way bodies are parsed.
 *
 * @since 1.0.0
 */
public enum ContentType {

	/**
	 * application/json.
	 */
	JSON("application/json"),
	/**
	 * application/xml.
	 */
	XML("application/xml"),
	/**
	 * text/plain.
	 */
	TEXT("text/plain"),
	/**
	 * application/x-www-form-urlencoded.
	 */
	FORM("application/x-www-form-urlencoded"),
	/**
	 * The content-type was defined and we don't want to override it.
	 */
	DEFINED(""),
	/**
	 * application/octet-stream.
	 */
	UNKNOWN("application/octet-stream");

	private final String mimeType;

	ContentType(String mimeType) {
		this.mimeType = mimeType;
	}

	public final String getMimeType() {
		return mimeType;
	}

}
