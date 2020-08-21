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

package org.springframework.cloud.contract.verifier.messaging.camel;

import java.util.Map;

import org.springframework.cloud.contract.verifier.util.MetadataUtil;
import org.springframework.cloud.contract.verifier.util.SpringCloudContractMetadata;

/**
 * Represents metadata for standalone communication.
 *
 * @author Marcin Grzejszczak
 * @since 3.0.0
 */
public class StandaloneMetadata implements SpringCloudContractMetadata {

	/**
	 * Key under which this metadata entry can be found in contract's metadata.
	 */
	public static final String METADATA_KEY = "standalone";

	/**
	 * Metadata for the setup message.
	 */
	private SetupMetadata setup = new SetupMetadata();

	/**
	 * Metadata for the input message.
	 */
	private MessageMetadata input = new MessageMetadata();

	/**
	 * Metadata for the output message.
	 */
	private MessageMetadata outputMessage = new MessageMetadata();

	public SetupMetadata getSetup() {
		return this.setup;
	}

	public void setSetup(SetupMetadata setup) {
		this.setup = setup;
	}

	public MessageMetadata getInput() {
		return this.input;
	}

	public void setInput(MessageMetadata input) {
		this.input = input;
	}

	public MessageMetadata getOutputMessage() {
		return this.outputMessage;
	}

	public void setOutputMessage(MessageMetadata outputMessage) {
		this.outputMessage = outputMessage;
	}

	public static StandaloneMetadata fromMetadata(Map<String, Object> metadata) {
		return MetadataUtil.fromMetadata(metadata, StandaloneMetadata.METADATA_KEY,
				new StandaloneMetadata());
	}

	@Override
	public String key() {
		return METADATA_KEY;
	}

	@Override
	public String description() {
		return "Metadata for standalone communication - with running middleware";
	}

	/**
	 * Message metadata.
	 */
	public static class MessageMetadata {

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

	/**
	 * Setup metadata.
	 */
	public static class SetupMetadata {

		/**
		 * If set, will be set as the full URI.
		 */
		private String options;

		public String getOptions() {
			return this.options;
		}

		public void setOptions(String options) {
			this.options = options;
		}

	}

}
