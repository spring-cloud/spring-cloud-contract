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

package contracts;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.security.auth.message.MessagePolicy;

import org.apache.camel.ConsumerTemplate;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.amqp.core.MessageProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.contract.verifier.converter.YamlContract;
import org.springframework.cloud.contract.verifier.messaging.MessageVerifier;
import org.springframework.cloud.contract.verifier.messaging.amqp.AmqpMetadata;
import org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierMessage;
import org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierMessageMetadata;
import org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierMessaging;
import org.springframework.cloud.contract.verifier.messaging.kafka.KafkaMetadata;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

/**
 * @author Marcin Grzejszczak
 */
@Configuration
@ConditionalOnProperty("MESSAGING_TYPE")
public class MessagingAutoConfig {

	@Bean
	public ContractVerifierMessaging<Message> contractVerifierMessaging(
			MessageVerifier<Message> exchange) {
		return new ContractVerifierCamelHelper(exchange);
	}

	@Bean
	MessageVerifier<Message> manualMessageVerifier(@Value("${MESSAGING_TYPE:}") String messagingType, Environment environment, ConsumerTemplate consumerTemplate) {
		return new MessageVerifier<Message>() {

			private final Logger log = LoggerFactory.getLogger(MessageVerifier.class);

			@Override
			public Message receive(String destination, long timeout, TimeUnit timeUnit, YamlContract yamlContract) {
				String uri = messagingType() + "://" + destination + additionalOptions(yamlContract);
				log.info("Camel URI [{}]", uri);
				Exchange exchange = consumerTemplate.receive(uri, timeUnit.toMillis(timeout));
				if (exchange == null) {
					return null;
				}
				return exchange.getMessage();
			}

			private String messagingType() {
				if (messagingType.equalsIgnoreCase("kafka")) {
					return "kafka";
				}
				return "rabbitmq";
			}

			private String additionalOptions(YamlContract contract) {
				if (contract == null) {
					return "";
				}
				if (messagingType.equalsIgnoreCase("kafka")) {
					return setKafkaOpts(contract);
				}
				return setRabbitOpts(contract);
			}

			private String setKafkaOpts(YamlContract contract) {
				String opts = defaultOpts(contract);
				KafkaMetadata metadata = KafkaMetadata.fromMetadata(contract.metadata);
				ContractVerifierMessageMetadata messageMetadata = ContractVerifierMessageMetadata.fromMetadata(contract.metadata);
				if (inputMessage(messageMetadata) && StringUtils.hasText(metadata.getInput().getConnectToBroker().getAdditionalOptions())) {
					return opts + "&" + metadata.getInput().getConnectToBroker().getAdditionalOptions();
				}
				else if (StringUtils.hasText(metadata.getOutputMessage().getConnectToBroker().getAdditionalOptions())) {
					return opts + "&" + metadata.getOutputMessage().getConnectToBroker().getAdditionalOptions();
				}
				return opts;
			}

			private String defaultOpts(YamlContract contract) {
				String consumerGroup = sameConsumerGroupForSameContract(contract);
				return "?brokers=" + environment.getRequiredProperty("SPRING_KAFKA_BOOTSTRAP_SERVERS") + "&autoOffsetReset=latest&groupId=" + consumerGroup + "&shutdownTimeout=5";
			}

			private String sameConsumerGroupForSameContract(YamlContract contract) {
				return contract.input.hashCode() + "_" + contract.outputMessage.hashCode();
			}

			private String setRabbitOpts(YamlContract contract) {
				String opts = "?addresses=" + environment.getRequiredProperty("SPRING_RABBITMQ_ADDRESSES");
				AmqpMetadata metadata = AmqpMetadata.fromMetadata(contract.metadata);
				ContractVerifierMessageMetadata messageMetadata = ContractVerifierMessageMetadata.fromMetadata(contract.metadata);
				if (inputMessage(messageMetadata) && StringUtils.hasText(metadata.getInput().getConnectToBroker().getAdditionalOptions())) {
					return opts + "&" + metadata.getInput().getConnectToBroker().getAdditionalOptions();
				}
				else if (StringUtils.hasText(metadata.getOutputMessage().getConnectToBroker().getAdditionalOptions())) {
					return opts + "&" + metadata.getOutputMessage().getConnectToBroker().getAdditionalOptions();
				}
				return defaultOpts(opts, metadata, messageMetadata);
			}

			private boolean inputMessage(ContractVerifierMessageMetadata messageMetadata) {
				return messageMetadata.getMessageType() == ContractVerifierMessageMetadata.MessageType.INPUT;
			}

			private String defaultOpts(String opts, AmqpMetadata amqpMetadata, ContractVerifierMessageMetadata messageMetadata) {
				AmqpMetadata.ConnectToBroker connectToBroker = inputMessage(messageMetadata) ? amqpMetadata.getInput().getConnectToBroker() : amqpMetadata.getOutputMessage().getConnectToBroker();
				MessageProperties messageProperties = inputMessage(messageMetadata) ? amqpMetadata.getInput().getMessageProperties() : amqpMetadata.getOutputMessage().getMessageProperties();
				if (StringUtils.hasText(connectToBroker.getDeclareQueueWithName())) {
					opts = opts + "&queue=" + connectToBroker.getDeclareQueueWithName();
				}
				if (messageProperties != null && StringUtils.hasText(messageProperties.getReceivedRoutingKey())) {
					opts = opts + "&routingKey=" + messageProperties.getReceivedRoutingKey();
				}
				return opts;
			}

			@Override
			public Message receive(String destination, YamlContract yamlContract) {
				return receive(destination, 5, TimeUnit.SECONDS, yamlContract);
			}

			@Override
			public void send(Message message, String destination, YamlContract yamlContract) {
				throw new UnsupportedOperationException("Currently supports only receiving");
			}

			@Override
			public void send(Object payload, Map headers, String destination, YamlContract yamlContract) {
				throw new UnsupportedOperationException("Currently supports only receiving");
			}
		};
	}

}

class ContractVerifierCamelHelper extends ContractVerifierMessaging<Message> {

	ContractVerifierCamelHelper(MessageVerifier<Message> exchange) {
		super(exchange);
	}

	@Override
	protected ContractVerifierMessage convert(Message receive) {
		if (receive == null) {
			return null;
		}
		return new ContractVerifierMessage(receive.getBody(), receive.getHeaders());
	}

}