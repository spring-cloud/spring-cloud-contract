/*
 * Copyright 2013-present the original author or authors.
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

import org.springframework.cloud.contract.verifier.messaging.avro.AvroMetadata;
import org.springframework.cloud.contract.verifier.util.MetadataUtil;
import org.springframework.cloud.contract.verifier.util.SpringCloudContractMetadata;

/**
 * Represents metadata for Kafka based communication.
 *
 * @author Marcin Grzejszczak
 * @since 3.0.0
 */
public final class KafkaMetadata implements SpringCloudContractMetadata {

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

    /**
     * Avro serialization metadata. Configures the schema used to
     * serialize/deserialize messages on this Kafka topic.
     */
    private AvroMetadata avro = new AvroMetadata();

    /**
     * Returns the input message metadata.
     *
     * @return the input metadata
     */
    public MessageKafkaMetadata getInput() {
        return this.input;
    }

    /**
     * Sets the input message metadata.
     *
     * @param value the input metadata
     */
    public void setInput(final MessageKafkaMetadata value) {
        this.input = value;
    }

    /**
     * Returns the output message metadata.
     *
     * @return the output message metadata
     */
    public MessageKafkaMetadata getOutputMessage() {
        return this.outputMessage;
    }

    /**
     * Sets the output message metadata.
     *
     * @param value the output message metadata
     */
    public void setOutputMessage(final MessageKafkaMetadata value) {
        this.outputMessage = value;
    }

    /**
     * Returns the Avro serialization metadata.
     *
     * @return the Avro metadata
     */
    public AvroMetadata getAvro() {
        return this.avro;
    }

    /**
     * Sets the Avro serialization metadata.
     *
     * @param value the Avro metadata
     */
    public void setAvro(final AvroMetadata value) {
        this.avro = value;
    }

    /**
     * Creates a {@link KafkaMetadata} instance from the given metadata map.
     *
     * @param metadata the contract metadata map
     * @return the parsed KafkaMetadata
     */
    public static KafkaMetadata fromMetadata(
            final Map<String, Object> metadata) {
        return MetadataUtil.fromMetadata(metadata,
                KafkaMetadata.METADATA_KEY, new KafkaMetadata());
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
    public static final class MessageKafkaMetadata {

        /**
         * Properties related to connecting to a real broker.
         */
        private ConnectToBroker connectToBroker = new ConnectToBroker();

        /**
         * Returns the broker connection properties.
         *
         * @return the connect-to-broker config
         */
        public ConnectToBroker getConnectToBroker() {
            return this.connectToBroker;
        }

        /**
         * Sets the broker connection properties.
         *
         * @param value the connect-to-broker config
         */
        public void setConnectToBroker(final ConnectToBroker value) {
            this.connectToBroker = value;
        }

    }

    /**
     * Options related to connecting to the real broker.
     */
    public static final class ConnectToBroker {

        /**
         * If set, will append any options to the existing ones that
         * define connection to the broker.
         */
        private String additionalOptions;

        /**
         * Returns the additional broker connection options.
         *
         * @return the additional options
         */
        public String getAdditionalOptions() {
            return this.additionalOptions;
        }

        /**
         * Sets the additional broker connection options.
         *
         * @param value the additional options
         */
        public void setAdditionalOptions(final String value) {
            this.additionalOptions = value;
        }

    }

}
