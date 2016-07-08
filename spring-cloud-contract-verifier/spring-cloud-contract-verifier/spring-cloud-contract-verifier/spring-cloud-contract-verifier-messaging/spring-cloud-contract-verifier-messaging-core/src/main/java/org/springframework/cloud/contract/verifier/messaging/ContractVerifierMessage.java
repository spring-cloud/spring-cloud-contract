/*
 *  Copyright 2013-2016 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.springframework.cloud.contract.verifier.messaging;

import java.util.Map;

/**
 * Describes a message. Contains payload and headers. A message can be converted
 * to another type (e.g. Spring Messaging Message)
 *
 * @author Marcin Grzejszczak
 */
public interface ContractVerifierMessage<PAYLOAD, TYPE_TO_CONVERT_INTO> {

	/**
	 * Returns a payload of type {@code PAYLOAD}
	 */
	PAYLOAD getPayload();

	/**
	 * Returns a map of headers
	 */
	Map<String, Object> getHeaders();

	/**
	 * Returns a header for a given key
	 */
	Object getHeader(String key);

	/**
	 * Converts the message to {@code TYPE_TO_CONVERT_INTO} type
	 */
	TYPE_TO_CONVERT_INTO convert();
}
