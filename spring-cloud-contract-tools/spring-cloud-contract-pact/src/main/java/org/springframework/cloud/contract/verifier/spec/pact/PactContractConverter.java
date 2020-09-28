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

package org.springframework.cloud.contract.verifier.spec.pact;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import au.com.dius.pact.core.model.DefaultPactReader;
import au.com.dius.pact.core.model.Pact;
import au.com.dius.pact.core.model.PactSpecVersion;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.messaging.MessagePact;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.core.util.Separators;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.cloud.contract.spec.Contract;
import org.springframework.cloud.contract.spec.ContractConverter;

/**
 * Converter of JSON PACT file.
 *
 * @author Marcin Grzejszczak
 * @author Tim Ysewyn
 * @author Stessy Delcroix
 * @since 1.1.0
 */
public class PactContractConverter implements ContractConverter<Collection<Pact<?>>> {

	private static final ObjectMapper OBJECT_MAPPER = ObjectMapperFactory.INSTANCE.getMapper();

	private final RequestResponseSCContractCreator requestResponseSCContractCreator = new RequestResponseSCContractCreator();

	private final MessagingSCContractCreator messagingSCContractCreator = new MessagingSCContractCreator();

	private final RequestResponsePactCreator requestResponsePactCreator = new RequestResponsePactCreator();

	private final MessagePactCreator messagePactCreator = new MessagePactCreator();

	@Override
	public boolean isAccepted(File file) {
		try {
			DefaultPactReader.INSTANCE.loadPact(file);
			return true;
		}
		catch (Exception e) {
			return false;
		}
	}

	@Override
	public Collection<Contract> convertFrom(File file) {
		Pact<?> pact = DefaultPactReader.INSTANCE.loadPact(file);
		if (pact instanceof RequestResponsePact) {
			return requestResponseSCContractCreator.convertFrom((RequestResponsePact) pact);
		}
		if (pact instanceof MessagePact) {
			return messagingSCContractCreator.convertFrom((MessagePact) pact);
		}
		throw new UnsupportedOperationException(
				"We currently don't support pact contracts of type" + pact.getClass().getSimpleName());
	}

	@Override
	public Collection<Pact<?>> convertTo(Collection<Contract> contracts) {
		List<Pact<?>> pactContracts = new ArrayList<>();
		Map<String, List<Contract>> groupedContracts = contracts.stream()
				.collect(Collectors.groupingBy(c -> NamingUtil.name(c).toString()));
		for (List<Contract> list : groupedContracts.values()) {
			List<Contract> httpOnly = list.stream().filter(c -> c.getRequest() != null).collect(Collectors.toList());
			List<Contract> messagingOnly = list.stream().filter(c -> c.getInput() != null).collect(Collectors.toList());
			RequestResponsePact responsePact = requestResponsePactCreator.createFromContract(httpOnly);
			if (responsePact != null) {
				pactContracts.add(responsePact);
			}
			MessagePact messagePact = messagePactCreator.createFromContract(messagingOnly);
			if (messagePact != null) {
				pactContracts.add(messagePact);
			}
		}
		return pactContracts;
	}

	@Override
	public Map<String, byte[]> store(Collection<Pact<?>> contracts) {
		return contracts.stream().collect(Collectors.toMap(this::name, c -> {
			try {
				return this.buildPrettyPrint(OBJECT_MAPPER.writeValueAsString(c.toMap(PactSpecVersion.V3))).getBytes();
			}
			catch (JsonProcessingException e) {
				throw new IllegalArgumentException("The pact contract is not a valid map", e);
			}
		}));
	}

	protected String name(Pact<?> contract) {
		return contract.getConsumer().getName() + "_" + contract.getProvider().getName() + "_"
				+ Math.abs(contract.hashCode()) + ".json";
	}

	private String buildPrettyPrint(String contract) {
		try {
			Object intermediateObjectForPrettyPrinting = OBJECT_MAPPER.reader().readValue(contract, Object.class);
			DefaultIndenter customIndenter = new DefaultIndenter("    ", "\n");
			return OBJECT_MAPPER
					.writer(new CustomPrettyPrinter().withArrayIndenter(customIndenter)
							.withObjectIndenter(customIndenter))
					.writeValueAsString(intermediateObjectForPrettyPrinting);
		}
		catch (IOException e) {
			throw new RuntimeException("WireMock response body could not be pretty printed");
		}
	}

	private static class CustomPrettyPrinter extends DefaultPrettyPrinter {

		@Override
		public CustomPrettyPrinter createInstance() {
			return new CustomPrettyPrinter();
		}

		@Override
		public DefaultPrettyPrinter withSeparators(Separators separators) {
			_separators = separators;
			_objectFieldValueSeparatorWithSpaces = separators.getObjectFieldValueSeparator() + " ";
			return this;
		}

	}

}
