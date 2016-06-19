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
 * Contract for creation of (@link ContractVerifierMessage}. You can create a message from
 * payload and headers or from some type (e.g. Spring Messaging Message).
 *
 * @author Marcin Grzejszczak
 */
public interface ContractVerifierMessageBuilder<PAYLOAD, TYPE_TO_CONVERT_INTO> {

	/**
	 * Creates a {@link ContractVerifierMessage} from payload and headers
	 */
	ContractVerifierMessage<PAYLOAD, TYPE_TO_CONVERT_INTO> create(PAYLOAD payload, Map<String, Object> headers);

	/**
	 * Creates a {@link ContractVerifierMessage} from the {@code TYPE_TO_CONVERT_INTO} type
	 */
	ContractVerifierMessage<PAYLOAD, TYPE_TO_CONVERT_INTO> create(TYPE_TO_CONVERT_INTO typeToConvertInto);
}
