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

package org.springframework.cloud.contract.verifier.messaging.amqp;

import java.util.Map;

import org.springframework.amqp.core.MessageProperties;
import org.springframework.cloud.contract.verifier.util.MetadataUtil;

/**
 * Represents metadata for AMQP based communication.
 *
 * @author Marcin Grzejszczak
 * @since 3.0.0
 */
public class AmqpMetadata {

	/**
	 * Key under which this metadata entry can be found in contract's metadata.
	 */
	public static final String METADATA_KEY = "amqp";

	/**
	 * Metadata for the input message.
	 */
	private MessageAmqpMetadata inputMessage = new MessageAmqpMetadata();

	/**
	 * Metadata for the output message.
	 */
	private MessageAmqpMetadata outputMessage = new MessageAmqpMetadata();

	public MessageAmqpMetadata getInputMessage() {
		return this.inputMessage;
	}

	public void setInputMessage(MessageAmqpMetadata inputMessage) {
		this.inputMessage = inputMessage;
	}

	public MessageAmqpMetadata getOutputMessage() {
		return this.outputMessage;
	}

	public void setOutputMessage(MessageAmqpMetadata outputMessage) {
		this.outputMessage = outputMessage;
	}

	public static AmqpMetadata fromMetadata(Map<String, Object> metadata) {
		return MetadataUtil.fromMetadata(metadata, AmqpMetadata.METADATA_KEY,
				new AmqpMetadata());
	}

	/**
	 * AMQP message metadata.
	 */
	public static class MessageAmqpMetadata {

		/**
		 * Spring AMQP message properties.
		 */
		private MessageProperties messageProperties;

		/**
		 * If set, will declare a queue with given name and bind it to the provided
		 * exchange from the contract.
		 */
		private String declareQueueWithName;

		public MessageProperties getMessageProperties() {
			return this.messageProperties;
		}

		public void setMessageProperties(MessageProperties messageProperties) {
			this.messageProperties = messageProperties;
		}

		public String getDeclareQueueWithName() {
			return declareQueueWithName;
		}

		public void setDeclareQueueWithName(String declareQueueWithName) {
			this.declareQueueWithName = declareQueueWithName;
		}

	}

}
