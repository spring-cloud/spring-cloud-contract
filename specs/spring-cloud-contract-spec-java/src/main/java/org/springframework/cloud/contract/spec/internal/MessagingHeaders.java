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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Contains most commonly used messaging headers.
 *
 * @author Marcin Grzejszczak
 * @author Tim Ysewyn
 * @since 1.1.2
 */
public class MessagingHeaders {

	private static final Log log = LogFactory.getLog(MessagingHeaders.class);

	public MessagingHeaders() {
		log.warn(
				"WARNING: MessagingHeaders shouldn't be instantiated. Use its static methods instead.");
	}

	/**
	 * The Content-Type header name of a message.
	 */
	public static final String MESSAGING_CONTENT_TYPE = "contentType";

	/**
	 * The Content-Type header name of a message.
	 * @return the Content-Type header name
	 * @deprecated Replaced by {@code MessagingHeaders.MESSAGING_CONTENT_TYPE}.
	 */
	@Deprecated
	public String messagingContentType() {
		return MESSAGING_CONTENT_TYPE;
	}

}
