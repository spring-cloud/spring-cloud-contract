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

package org.springframework.cloud.contract.verifier.messaging.internal;

import java.util.Map;

import org.springframework.cloud.contract.verifier.util.MetadataUtil;
import org.springframework.cloud.contract.verifier.util.SpringCloudContractMetadata;

/**
 * Metadata representation of the Contract Verifier messaging.
 *
 * @author Marcin Grzejszczak
 * @since 3.0.0
 */
public class ContractVerifierMessageMetadata implements SpringCloudContractMetadata {

	/**
	 * Metadata entry in the contract.
	 */
	public static final String METADATA_KEY = "verifierMessage";

	private MessageType messageType;

	public ContractVerifierMessageMetadata(MessageType messageType) {
		this.messageType = messageType;
	}

	public ContractVerifierMessageMetadata() {
	}

	public MessageType getMessageType() {
		return this.messageType;
	}

	public void setMessageType(MessageType messageType) {
		this.messageType = messageType;
	}

	public static ContractVerifierMessageMetadata fromMetadata(
			Map<String, Object> metadata) {
		return MetadataUtil.fromMetadata(metadata, METADATA_KEY,
				new ContractVerifierMessageMetadata());
	}

	@Override
	public String key() {
		return METADATA_KEY;
	}

	@Override
	public String description() {
		return "Internal metadata entries used by the framework";
	}

	/**
	 * Type of a message.
	 */
	public enum MessageType {

		/**
		 * Setup message.
		 */
		SETUP,

		/**
		 * Input message.
		 */
		INPUT,

		/**
		 * Output message.
		 */
		OUTPUT

	}

}
