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

package org.springframework.cloud.contract.verifier.file;

import java.io.File;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.cloud.contract.spec.Contract;
import org.springframework.cloud.contract.spec.internal.DslProperty;
import org.springframework.cloud.contract.spec.internal.Header;
import org.springframework.cloud.contract.spec.internal.Headers;
import org.springframework.cloud.contract.spec.internal.Input;
import org.springframework.cloud.contract.spec.internal.OutputMessage;
import org.springframework.cloud.contract.spec.internal.Request;
import org.springframework.cloud.contract.spec.internal.Response;
import org.springframework.cloud.contract.verifier.util.ContentType;
import org.springframework.util.Assert;

import static org.springframework.cloud.contract.verifier.util.ContentType.DEFINED;
import static org.springframework.cloud.contract.verifier.util.ContentType.JSON;
import static org.springframework.cloud.contract.verifier.util.ContentType.UNKNOWN;
import static org.springframework.cloud.contract.verifier.util.ContentType.XML;
import static org.springframework.cloud.contract.verifier.util.ContentUtils.evaluateClientSideContentType;
import static org.springframework.cloud.contract.verifier.util.ContentUtils.evaluateServerSideContentType;
import static org.springframework.cloud.contract.verifier.util.NamesUtil.afterLast;
import static org.springframework.cloud.contract.verifier.util.NamesUtil.camelCase;
import static org.springframework.cloud.contract.verifier.util.NamesUtil.convertIllegalMethodNameChars;
import static org.springframework.cloud.contract.verifier.util.NamesUtil.convertIllegalPackageChars;
import static org.springframework.cloud.contract.verifier.util.NamesUtil.isEmpty;
import static org.springframework.cloud.contract.verifier.util.NamesUtil.toLastDot;

public class SingleContractMetadata {

	private static final Logger log = LoggerFactory.getLogger(SingleContractMetadata.class);

	private final ContractMetadata contractMetadata;

	private final Path stubsPath;

	private final Contract contract;

	private final List<Contract> allContracts;

	private final String definedInputStubContentType;

	private final ContentType inputStubContentType;

	private final ContentType evaluatedInputStubContentType;

	private final String definedOutputStubContentType;

	private final ContentType outputStubContentType;

	private final ContentType evaluatedOutputStubContentType;

	private final String definedInputTestContentType;

	private final ContentType inputTestContentType;

	private final ContentType evaluatedInputTestContentType;

	private final String definedOutputTestContentType;

	private final ContentType outputTestContentType;

	private final ContentType evaluatedOutputTestContentType;

	private String methodName;

	private final boolean http;

	public SingleContractMetadata(Contract currentContract, ContractMetadata contractMetadata) {
		Assert.notNull(currentContract, "Contract must not be null");
		this.allContracts = contractMetadata.getConvertedContract();
		this.contract = currentContract;
		this.contractMetadata = contractMetadata;
		Headers inputHeaders = inputHeaders(currentContract);
		DslProperty<?> inputBody = inputBody(currentContract);
		Headers outputHeaders = outputHeaders(currentContract);
		DslProperty<?> outputBody = outputBody(currentContract);
		Header inputContentType = contentTypeHeader(inputHeaders);
		Header outputContentType = contentTypeHeader(outputHeaders);
		this.definedInputTestContentType = Optional.ofNullable(inputContentType).map(DslProperty::getServerValue)
				.map(Object::toString).orElse("");
		this.evaluatedInputTestContentType = tryToEvaluateTestContentType(inputHeaders, inputBody);
		this.inputTestContentType = inputBody != null ? this.evaluatedInputTestContentType : UNKNOWN;
		this.definedOutputTestContentType = Optional.ofNullable(outputContentType).map(DslProperty::getServerValue)
				.map(Object::toString).orElse("");
		this.evaluatedOutputTestContentType = tryToEvaluateTestContentType(outputHeaders, outputBody);
		this.outputTestContentType = outputBody != null ? this.evaluatedOutputTestContentType : UNKNOWN;
		this.definedInputStubContentType = Optional.ofNullable(inputContentType).map(DslProperty::getClientValue)
				.map(Object::toString).orElse("");
		this.evaluatedInputStubContentType = tryToEvaluateStubContentType(inputHeaders, inputBody);
		this.inputStubContentType = inputBody != null ? this.evaluatedInputStubContentType : UNKNOWN;
		this.definedOutputStubContentType = Optional.ofNullable(outputContentType).map(DslProperty::getClientValue)
				.map(Object::toString).orElse("");
		this.evaluatedOutputStubContentType = tryToEvaluateStubContentType(outputHeaders, outputBody);
		this.outputStubContentType = outputBody != null ? this.evaluatedOutputStubContentType : UNKNOWN;
		this.http = currentContract.getRequest() != null;
		this.stubsPath = contractMetadata.getPath();
	}

	private Header contentTypeHeader(Headers headers) {
		return headers == null ? null : headers.getEntries().stream()
				.filter(it -> "Content-Type".equalsIgnoreCase(it.getName())).findFirst().orElse(null);
	}

	private ContentType tryToEvaluateStubContentType(Headers mainHeaders, DslProperty<?> body) {
		Object clientValue = Optional.ofNullable(body).map(DslProperty::getClientValue).orElse(null);
		ContentType contentType = evaluateClientSideContentType(mainHeaders, clientValue);
		if (contentType == DEFINED || contentType == UNKNOWN) {
			// try to retrieve from the other side (e.g. stub side was a regex, but test
			// side is concrete)
			Object serverValue = Optional.ofNullable(body).map(DslProperty::getServerValue).orElse(null);
			return evaluateServerSideContentType(mainHeaders, serverValue);
		}
		return contentType;
	}

	private ContentType tryToEvaluateTestContentType(Headers mainHeaders, DslProperty<?> body) {
		Object serverValue = Optional.ofNullable(body).map(DslProperty::getServerValue).orElse(null);
		ContentType contentType = evaluateClientSideContentType(mainHeaders, serverValue);
		if (contentType == DEFINED || contentType == UNKNOWN) {
			// try to retrieve from the other side (e.g. stub side was a regex, but test
			// side is concrete)
			Object clientValue = Optional.ofNullable(body).map(DslProperty::getClientValue).orElse(null);
			return evaluateServerSideContentType(mainHeaders, clientValue);
		}
		return contentType;
	}

	public boolean isJson() {
		return this.inputTestContentType.equals(JSON) || this.outputTestContentType.equals(JSON)
				|| this.inputStubContentType.equals(JSON) || this.outputStubContentType.equals(JSON);
	}

	public boolean evaluatesToJson() {
		return isJson() || this.evaluatedInputTestContentType.equals(JSON)
				|| this.evaluatedOutputTestContentType.equals(JSON) || this.evaluatedInputStubContentType.equals(JSON)
				|| this.evaluatedOutputStubContentType.equals(JSON);
	}

	public boolean isIgnored() {
		return this.contract.getIgnored() || this.contractMetadata.getIgnored();
	}

	public boolean isXml() {
		return this.inputTestContentType.equals(XML) || this.outputTestContentType.equals(XML)
				|| this.inputStubContentType.equals(XML) || this.outputStubContentType.equals(XML);
	}

	public boolean isHttp() {
		return this.http;
	}

	public boolean isInProgress() {
		return this.contract.isInProgress();
	}

	public boolean isMessaging() {
		return !isHttp();
	}

	private DslProperty<?> inputBody(Contract contract) {
		return Optional.ofNullable(contract.getRequest()).map(Request::getBody).map(DslProperty.class::cast)
				.orElseGet(() -> Optional.ofNullable(contract.getInput()).map(Input::getMessageBody).orElse(null));
	}

	private Headers inputHeaders(Contract contract) {
		return Optional.ofNullable(contract.getRequest()).map(Request::getHeaders)
				.orElseGet(() -> Optional.ofNullable(contract.getInput()).map(Input::getMessageHeaders).orElse(null));
	}

	private DslProperty<?> outputBody(Contract contract) {
		return Optional.ofNullable(contract.getResponse()).map(Response::getBody).map(DslProperty.class::cast)
				.orElseGet(() -> Optional.ofNullable(contract.getOutputMessage()).map(OutputMessage::getBody)
						.orElse(null));
	}

	private Headers outputHeaders(Contract contract) {
		return Optional.ofNullable(contract.getResponse()).map(Response::getHeaders).orElseGet(
				() -> Optional.ofNullable(contract.getOutputMessage()).map(OutputMessage::getHeaders).orElse(null));
	}

	public String methodName() {
		if (this.methodName == null) {
			this.methodName = calculateMethodName();
		}
		return this.methodName;
	}

	private String calculateMethodName() {
		if (!isEmpty(contract.getName())) {
			String name = camelCase(convertIllegalPackageChars(contract.getName()));
			log.debug("Overriding the default test name with [{}]", name);
			return name;
		}
		if (allContracts.size() > 1) {
			int index = allContracts.indexOf(getContract());
			String name = String.format("%s_%d", camelCasedMethodFromFileName(stubsPath), index);
			log.debug("Scenario found. The method name will be [{}]", name);
			return name;
		}
		String name = camelCasedMethodFromFileName(stubsPath);
		log.debug("The method name will be [{}]", name);
		return name;
	}

	private static String camelCasedMethodFromFileName(Path stubsPath) {
		return camelCase(convertIllegalMethodNameChars(toLastDot(afterLast(stubsPath.toString(), File.separator))));
	}

	public ContractMetadata getContractMetadata() {
		return contractMetadata;
	}

	public Contract getContract() {
		return contract;
	}

	public Collection<Contract> getAllContracts() {
		return allContracts;
	}

	public String getDefinedInputStubContentType() {
		return definedInputStubContentType;
	}

	public ContentType getInputStubContentType() {
		return inputStubContentType;
	}

	public ContentType getEvaluatedInputStubContentType() {
		return evaluatedInputStubContentType;
	}

	public String getDefinedOutputStubContentType() {
		return definedOutputStubContentType;
	}

	public ContentType getEvaluatedOutputStubContentType() {
		return evaluatedOutputStubContentType;
	}

	public String getDefinedInputTestContentType() {
		return definedInputTestContentType;
	}

	public ContentType getInputTestContentType() {
		return inputTestContentType;
	}

	public String getDefinedOutputTestContentType() {
		return definedOutputTestContentType;
	}

	public ContentType getOutputTestContentType() {
		return outputTestContentType;
	}

	public ContentType getEvaluatedOutputTestContentType() {
		return evaluatedOutputTestContentType;
	}

}
