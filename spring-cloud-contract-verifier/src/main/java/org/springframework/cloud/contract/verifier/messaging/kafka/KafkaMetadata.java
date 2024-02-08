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

package org.springframework.cloud.contract.verifier.messaging.kafka;

import java.util.Map;

import org.springframework.cloud.contract.verifier.util.MetadataUtil;
import org.springframework.cloud.contract.verifier.util.SpringCloudContractMetadata;

/**
 * Represents metadata for Kafka based communication.
 *
 * @author Marcin Grzejszczak
 * @since 3.0.0
 */
public class KafkaMetadata implements SpringCloudContractMetadata {

	/**
	 * Key under which this metadata entry can be found in contract's metadata.
	 */
	public static final String METADATA_KEY = "kafka";

	/**
	 * Metadata for the input message.
	 */
	private MessageKafkaMetadata input = new MessageKafkaMetadata();

	/**
	 * Metadata for the output message.
	 */
	private MessageKafkaMetadata outputMessage = new MessageKafkaMetadata();

	public MessageKafkaMetadata getInput() {
		return this.input;
	}

	public void setInput(MessageKafkaMetadata input) {
		this.input = input;
	}

	public MessageKafkaMetadata getOutputMessage() {
		return this.outputMessage;
	}

	public void setOutputMessage(MessageKafkaMetadata outputMessage) {
		this.outputMessage = outputMessage;
	}

	public static KafkaMetadata fromMetadata(Map<String, Object> metadata) {
		return MetadataUtil.fromMetadata(metadata, KafkaMetadata.METADATA_KEY, new KafkaMetadata());
	}

	@Override
	public String key() {
		return METADATA_KEY;
	}

	@Override
	public String description() {
		return "Metadata for Kafka based communication";
	}

	/**
	 * Kafka message metadata.
	 */
	public static class MessageKafkaMetadata {

		/**
		 * Properties related to connecting to a real broker.
		 */
		private ConnectToBroker connectToBroker = new ConnectToBroker();

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

		public String getAdditionalOptions() {
			return this.additionalOptions;
		}

		public void setAdditionalOptions(String additionalOptions) {
			this.additionalOptions = additionalOptions;
		}

	}

}
