package org.springframework.cloud.contract.verifier.builder;

import java.util.Map;

import org.springframework.cloud.contract.spec.internal.NamedProperty;
import org.springframework.cloud.contract.spec.internal.Request;
import org.springframework.cloud.contract.verifier.config.TestFramework;
import org.springframework.cloud.contract.verifier.file.SingleContractMetadata;
import org.springframework.cloud.contract.verifier.util.ContentUtils;
import org.springframework.cloud.contract.verifier.util.MapConverter;

public class SpockWebTestClientMultipartGiven implements Given, WebTestClientAcceptor {

	private final BlockBuilder blockBuilder;

	private final GeneratedClassMetaData generatedClassMetaData;

	private final BodyReader bodyReader;

	private final BodyParser bodyParser;

	SpockWebTestClientMultipartGiven(BlockBuilder blockBuilder, GeneratedClassMetaData generatedClassMetaData,
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
		return ContentUtils.getGroovyMultipartFileParameterContent(propertyName, propertyValue,
				fileProp -> this.bodyReader.readBytesFromFileString(metadata, fileProp, CommunicationType.REQUEST));
	}

	private String getParameterString(Map.Entry<String, Object> parameter) {
		return ".param(" + this.bodyParser.quotedShortText(parameter.getKey()) + ", "
				+ this.bodyParser.quotedShortText(MapConverter.getTestSideValuesForNonBody(parameter.getValue())) + ")";
	}

	@Override
	public boolean accept(SingleContractMetadata metadata) {
		Request request = metadata.getContract().getRequest();
		return request != null && request.getMultipart() != null && acceptType(this.generatedClassMetaData)
				&& this.generatedClassMetaData.configProperties.getTestFramework() == TestFramework.SPOCK;
	}

}
