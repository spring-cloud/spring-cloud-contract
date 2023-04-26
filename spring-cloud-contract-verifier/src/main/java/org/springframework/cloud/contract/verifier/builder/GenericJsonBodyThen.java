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

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.apache.commons.text.StringEscapeUtils;

import org.springframework.cloud.contract.spec.ContractTemplate;
import org.springframework.cloud.contract.spec.internal.BodyMatchers;
import org.springframework.cloud.contract.spec.internal.ExecutionProperty;
import org.springframework.cloud.contract.spec.internal.FromFileProperty;
import org.springframework.cloud.contract.verifier.config.TestFramework;
import org.springframework.cloud.contract.verifier.file.SingleContractMetadata;
import org.springframework.cloud.contract.verifier.template.HandlebarsTemplateProcessor;
import org.springframework.cloud.contract.verifier.template.TemplateProcessor;
import org.springframework.cloud.contract.verifier.util.ContentType;
import org.springframework.cloud.contract.verifier.util.MapConverter;
import org.springframework.util.StringUtils;

import static org.springframework.cloud.contract.verifier.util.ContentType.DEFINED;
import static org.springframework.cloud.contract.verifier.util.ContentType.FORM;
import static org.springframework.cloud.contract.verifier.util.ContentType.JSON;
import static org.springframework.cloud.contract.verifier.util.ContentType.TEXT;

class GenericJsonBodyThen implements Then {

	private final BlockBuilder blockBuilder;

	private final GeneratedClassMetaData generatedClassMetaData;

	private final BodyParser bodyParser;

	private final BodyAssertionLineCreator bodyAssertionLineCreator;

	private final TemplateProcessor templateProcessor;

	private final ContractTemplate contractTemplate;

	private final ComparisonBuilder comparisonBuilder;

	GenericJsonBodyThen(BlockBuilder blockBuilder, GeneratedClassMetaData metaData, BodyParser bodyParser,
			ComparisonBuilder comparisonBuilder) {
		this.blockBuilder = blockBuilder;
		this.bodyParser = bodyParser;
		this.comparisonBuilder = comparisonBuilder;
		this.bodyAssertionLineCreator = new BodyAssertionLineCreator(blockBuilder, metaData,
				this.bodyParser.byteArrayString(), this.comparisonBuilder);
		this.generatedClassMetaData = metaData;
		this.templateProcessor = new HandlebarsTemplateProcessor();
		this.contractTemplate = new HandlebarsTemplateProcessor();
	}

	@Override
	public MethodVisitor<Then> apply(SingleContractMetadata metadata) {
		BodyMatchers bodyMatchers = this.bodyParser.responseBodyMatchers(metadata);
		Object convertedResponseBody = this.bodyParser.convertResponseBody(metadata);
		ContentType contentType = metadata.getOutputTestContentType();
		if (TEXT != contentType && FORM != contentType && DEFINED != contentType) {
			boolean dontParseStrings = contentType == JSON && convertedResponseBody instanceof Map;
			Function parsingClosure = dontParseStrings ? Function.identity() : MapConverter.JSON_PARSING_FUNCTION;
			convertedResponseBody = MapConverter.getTestSideValues(convertedResponseBody, parsingClosure);
		}
		else {
			convertedResponseBody = StringEscapeUtils.escapeJava(convertedResponseBody.toString());
		}
		addJsonBodyVerification(metadata, convertedResponseBody, bodyMatchers);
		return this;
	}

	private void addJsonBodyVerification(SingleContractMetadata contractMetadata, Object responseBody,
			BodyMatchers bodyMatchers) {
		JsonBodyVerificationBuilder jsonBodyVerificationBuilder = new JsonBodyVerificationBuilder(
				this.generatedClassMetaData.configProperties.getAssertJsonSize(), this.templateProcessor,
				this.contractTemplate, contractMetadata.getContract(), Optional.of(this.blockBuilder.getLineEnding()),
				bodyParser::postProcessJsonPath);
		// TODO: Refactor spock from should comment out bdd blocks
		Object convertedResponseBody = jsonBodyVerificationBuilder.addJsonResponseBodyCheck(this.blockBuilder,
				responseBody, bodyMatchers, this.bodyParser.responseAsString(),
				this.generatedClassMetaData.configProperties.getTestFramework() != TestFramework.SPOCK);
		if (!(convertedResponseBody instanceof Map || convertedResponseBody instanceof List
				|| convertedResponseBody instanceof ExecutionProperty)) {
			simpleTextResponseBodyCheck(contractMetadata, convertedResponseBody);
		}
		processBodyElement("", "", convertedResponseBody);
	}

	private void processBodyElement(String oldProp, String property, Object value) {
		String propDiff = subtract(property, oldProp);
		String prop = wrappedWithBracketsForDottedProp(propDiff);
		String mergedProp = StringUtils.hasText(property) ? oldProp + "." + prop : "";
		if (value instanceof ExecutionProperty) {
			processBodyElement(mergedProp, (ExecutionProperty) value);
		}
		else if (value instanceof Map.Entry) {
			processBodyElement(mergedProp, (Map.Entry) value);
		}
		else if (value instanceof Map) {
			processBodyElement(mergedProp, (Map) value);
		}
		else if (value instanceof List) {
			processBodyElement(mergedProp, (List) value);
		}
	}

	private void processBodyElement(String property, ExecutionProperty exec) {
		this.blockBuilder.addLineWithEnding(
				exec.insertValue(this.bodyParser.postProcessJsonPath("parsedJson.read(\"$" + property + "\")")));
	}

	private void processBodyElement(String property, Map.Entry entry) {
		processBodyElement(property, getMapKeyReferenceString(property, entry), entry.getValue());
	}

	private void processBodyElement(String property, Map map) {
		map.entrySet().forEach(o -> processBodyElement(property, (Map.Entry) o));
	}

	private void processBodyElement(String property, List list) {
		Iterator iterator = list.iterator();
		int index = -1;
		while (iterator.hasNext()) {
			Object listElement = iterator.next();
			index = index + 1;
			String prop = getPropertyInListString(property, index);
			processBodyElement(property, prop, listElement);
		}
	}

	private String getPropertyInListString(String property, Integer listIndex) {
		return property + "[" + listIndex + "]";
	}

	private String getMapKeyReferenceString(String property, Map.Entry entry) {
		return provideProperJsonPathNotation(property) + "." + entry.getKey();
	}

	private String provideProperJsonPathNotation(String property) {
		return property.replaceAll("(get\\(\\\\\")(.*)(\\\\\"\\))", "$2");
	}

	private String wrappedWithBracketsForDottedProp(String key) {
		String remindingKey = trailingKey(key);
		if (remindingKey.contains(".")) {
			return "['" + remindingKey + "']";
		}
		return remindingKey;
	}

	private String trailingKey(String key) {
		if (key.startsWith(".")) {
			return key.substring(1);
		}
		return key;
	}

	private String subtract(String self, String text) {
		int index = self.indexOf(text);
		if (index == -1) {
			return self;
		}
		int end = index + text.length();
		if (self.length() > end) {
			return self.substring(0, index) + self.substring(end);
		}
		return self.substring(0, index);
	}

	private void simpleTextResponseBodyCheck(SingleContractMetadata metadata, Object convertedResponseBody) {
		this.blockBuilder.addLineWithEnding(getSimpleResponseBodyString(this.bodyParser.responseAsString()));
		this.bodyAssertionLineCreator.appendBodyAssertionLine(metadata, "", convertedResponseBody);
		this.blockBuilder.addEndingIfNotPresent();
	}

	private String getSimpleResponseBodyString(String responseString) {
		return "String responseBody = " + responseString + this.blockBuilder.getLineEnding();
	}

	@Override
	public boolean accept(SingleContractMetadata metadata) {
		Object responseBody = this.bodyParser.responseBody(metadata).getServerValue();
		if (responseBody instanceof FromFileProperty) {
			return !((FromFileProperty) responseBody).isByte();
		}
		ContentType outputTestContentType = metadata.getOutputTestContentType();
		return JSON == outputTestContentType || mostLikelyJson(outputTestContentType, metadata);
	}

	private boolean mostLikelyJson(ContentType outputTestContentType, SingleContractMetadata metadata) {
		return DEFINED == outputTestContentType && metadata.evaluatesToJson();
	}

}
