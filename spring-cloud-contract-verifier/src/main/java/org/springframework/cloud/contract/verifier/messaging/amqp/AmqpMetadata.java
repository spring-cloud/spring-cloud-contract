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

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.amqp.core.MessageProperties;
import org.springframework.cloud.contract.verifier.util.MetadataUtil;
import org.springframework.cloud.contract.verifier.util.SpringCloudContractMetadata;

/**
 * Represents metadata for AMQP based communication.
 *
 * @author Marcin Grzejszczak
 * @since 3.0.0
 */
public class AmqpMetadata implements SpringCloudContractMetadata {

	/**
	 * Key under which this metadata entry can be found in contract's metadata.
	 */
	public static final String METADATA_KEY = "amqp";

	/**
	 * Metadata for the input message.
	 */
	private MessageAmqpMetadata input = new MessageAmqpMetadata();

	/**
	 * Metadata for the output message.
	 */
	private MessageAmqpMetadata outputMessage = new MessageAmqpMetadata();

	public MessageAmqpMetadata getInput() {
		return this.input;
	}

	public void setInput(MessageAmqpMetadata input) {
		this.input = input;
	}

	public MessageAmqpMetadata getOutputMessage() {
		return this.outputMessage;
	}

	public void setOutputMessage(MessageAmqpMetadata outputMessage) {
		this.outputMessage = outputMessage;
	}

	public static AmqpMetadata fromMetadata(Map<String, Object> metadata) {
		return MetadataUtil.fromMetadata(metadata, AmqpMetadata.METADATA_KEY, new AmqpMetadata());
	}

	@Override
	public String key() {
		return METADATA_KEY;
	}

	@Override
	public String description() {
		return "Metadata for AMQP based communication";
	}

	@Override
	public List<Class> additionalClassesToLookAt() {
		return Collections.singletonList(MessageProperties.class);
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
		 * Properties related to connecting to a real broker.
		 */
		private ConnectToBroker connectToBroker = new ConnectToBroker();

		public MessageProperties getMessageProperties() {
			return this.messageProperties;
		}

		public void setMessageProperties(MessageProperties messageProperties) {
			this.messageProperties = messageProperties;
		}

		public ConnectToBroker getConnectToBroker() {
			return this.connectToBroker;
		}

		public void setConnectToBroker(ConnectToBroker connectToBroker) {
			this.connectToBroker = connectToBroker;
		}

	}

	/**
	 * Options related to connecting to the real broker.
	 */
	public static class ConnectToBroker {

		/**
		 * If set, will append any options to the existing ones that define connection to
		 * the broker.
		 */
		private String additionalOptions;

		/**
		 * If set, will declare a queue with given name and bind it to the provided
		 * exchange from the contract.
		 */
		private String declareQueueWithName;

		public String getAdditionalOptions() {
			return this.additionalOptions;
		}

		public void setAdditionalOptions(String additionalOptions) {
			this.additionalOptions = additionalOptions;
		}

		public String getDeclareQueueWithName() {
			return declareQueueWithName;
		}

		public void setDeclareQueueWithName(String declareQueueWithName) {
			this.declareQueueWithName = declareQueueWithName;
		}

	}

}
