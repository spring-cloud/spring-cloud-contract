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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Contains most commonly used media types.
 *
 * @author Marcin Grzejszczak
 * @author Tim Ysewyn
 * @since 1.0.2
 */
public final class MediaTypes {

	private static final Log log = LogFactory.getLog(MediaTypes.class);

	private MediaTypes() {
		throw new IllegalStateException("You can't instantiate an utility class");
	}

	/**
	 * Public constant for that includes all media ranges (i.e. "*&#47;*").
	 */
	public static final String ALL_VALUE = "*/*";

	/**
	 * Public constant for {@code application/atom+xml}.
	 */
	public static final String APPLICATION_ATOM_XML = "application/atom+xml";

	/**
	 * Public constant for {@code application/x-www-form-urlencoded}.
	 */
	public static final String APPLICATION_FORM_URLENCODED = "application/x-www-form-urlencoded";

	/**
	 * Public constant for {@code application/json}.
	 */
	public static final String APPLICATION_JSON = "application/json";

	/**
	 * Public constant for {@code application/json;charset=UTF-8}.
	 */
	public static final String APPLICATION_JSON_UTF8 = APPLICATION_JSON + ";charset=UTF-8";

	/**
	 * Public constant for {@code application/octet-stream}.
	 */
	public static final String APPLICATION_OCTET_STREAM = "application/octet-stream";

	/**
	 * Public constant for {@code application/pdf}.
	 */
	public static final String APPLICATION_PDF = "application/pdf";

	/**
	 * Public constant for {@code application/xhtml+xml}.
	 */
	public static final String APPLICATION_XHTML_XML = "application/xhtml+xml";

	/**
	 * Public constant for {@code application/xml}.
	 */
	public static final String APPLICATION_XML = "application/xml";

	/**
	 * Public constant for {@code image/gif}.
	 */
	public static final String IMAGE_GIF = "image/gif";

	/**
	 * Public constant for {@code image/jpeg}.
	 */
	public static final String IMAGE_JPEG = "image/jpeg";

	/**
	 * Public constant for {@code image/png}.
	 */
	public static final String IMAGE_PNG = "image/png";

	/**
	 * Public constant for {@code multipart/form-data}.
	 */
	public static final String MULTIPART_FORM_DATA = "multipart/form-data";

	/**
	 * Public constant for {@code text/html}.
	 */
	public static final String TEXT_HTML = "text/html";

	/**
	 * Public constant for {@code text/markdown}.
	 */
	public static final String TEXT_MARKDOWN = "text/markdown";

	/**
	 * Public constant for {@code text/plain}.
	 */
	public static final String TEXT_PLAIN = "text/plain";

	/**
	 * Public constant for {@code text/xml}.
	 */
	public static final String TEXT_XML = "text/xml";

}
