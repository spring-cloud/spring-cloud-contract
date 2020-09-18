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

package org.springframework.cloud.contract.verifier.builder;

import java.util.Map;

import org.springframework.cloud.contract.spec.internal.NamedProperty;
import org.springframework.cloud.contract.spec.internal.Request;
import org.springframework.cloud.contract.verifier.config.TestFramework;
import org.springframework.cloud.contract.verifier.file.SingleContractMetadata;
import org.springframework.cloud.contract.verifier.util.MapConverter;

import static org.springframework.cloud.contract.verifier.util.ContentUtils.getJavaMultipartFileParameterContent;

class JavaMultipartGiven implements Given, RestAssuredAcceptor {

	private final BlockBuilder blockBuilder;

	private final GeneratedClassMetaData generatedClassMetaData;

	private final BodyReader bodyReader;

	private final BodyParser bodyParser;

	JavaMultipartGiven(BlockBuilder blockBuilder, GeneratedClassMetaData generatedClassMetaData,
			BodyParser bodyParser) {
		this.blockBuilder = blockBuilder;
		this.bodyReader = new BodyReader(generatedClassMetaData);
		this.bodyParser = bodyParser;
		this.generatedClassMetaData = generatedClassMetaData;
	}

	@Override
	public MethodVisitor<Given> apply(SingleContractMetadata metadata) {
		getMultipartParameters(metadata).entrySet()
				.forEach(entry -> this.blockBuilder.addLine(getMultipartParameterLine(metadata, entry)));
		return this;
	}

	private String getMultipartParameterLine(SingleContractMetadata metadata, Map.Entry<String, Object> parameter) {
		if (parameter.getValue() instanceof NamedProperty) {
			return ".multiPart(" + getMultipartFileParameterContent(metadata, parameter.getKey(),
					(NamedProperty) parameter.getValue()) + ")";
		}
		return getParameterString(parameter);
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> getMultipartParameters(SingleContractMetadata metadata) {
		return (Map<String, Object>) metadata.getContract().getRequest().getMultipart().getServerValue();
	}

	private String getMultipartFileParameterContent(SingleContractMetadata metadata, String propertyName,
			NamedProperty propertyValue) {
		return getJavaMultipartFileParameterContent(propertyName, propertyValue,
				fileProp -> this.bodyReader.readBytesFromFileString(metadata, fileProp, CommunicationType.REQUEST));
	}

	private String getParameterString(Map.Entry<String, Object> parameter) {
		return ".param(" + this.bodyParser.quotedShortText(parameter.getKey()) + ", "
				+ this.bodyParser.quotedShortText(MapConverter.getTestSideValuesForNonBody(parameter.getValue())) + ")";
	}

	@Override
	public boolean accept(SingleContractMetadata metadata) {
		Request request = metadata.getContract().getRequest();
		return request != null && request.getMultipart() != null && acceptType(this.generatedClassMetaData, metadata)
				&& this.generatedClassMetaData.configProperties.getTestFramework() != TestFramework.SPOCK;
	}

}
