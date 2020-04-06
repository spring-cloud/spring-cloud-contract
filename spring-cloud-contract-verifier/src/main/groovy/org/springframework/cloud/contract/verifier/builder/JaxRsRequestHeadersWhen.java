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
import java.util.Set;

import org.springframework.cloud.contract.spec.internal.ExecutionProperty;
import org.springframework.cloud.contract.spec.internal.Header;
import org.springframework.cloud.contract.spec.internal.MatchingStrategy;
import org.springframework.cloud.contract.spec.internal.Request;
import org.springframework.cloud.contract.verifier.file.SingleContractMetadata;
import org.springframework.cloud.contract.verifier.util.MapConverter;

class JaxRsRequestHeadersWhen implements When {

	private final BlockBuilder blockBuilder;

	private final BodyParser bodyParser;

	JaxRsRequestHeadersWhen(BlockBuilder blockBuilder, BodyParser bodyParser) {
		this.blockBuilder = blockBuilder;
		this.bodyParser = bodyParser;
	}

	@Override
	public MethodVisitor<When> apply(SingleContractMetadata metadata) {
		appendHeaders(metadata.getContract().getRequest());
		return this;
	}

	private void appendHeaders(Request request) {
		Iterator<Header> iterator = request.getHeaders().getEntries().stream()
				.filter(header -> !headerToIgnore(header)).iterator();
		while (iterator.hasNext()) {
			Header header = iterator.next();
			String text = ".header(\"" + header.getName() + "\", " + headerValue(header)
					+ ")";
			if (iterator.hasNext()) {
				this.blockBuilder.addLine(text);
			}
			else {
				this.blockBuilder.addIndented(text);
			}
		}
	}

	private String headerValue(Header header) {
		Object headerServerValue = header.getServerValue();
		if (headerServerValue instanceof ExecutionProperty) {
			return ((ExecutionProperty) headerServerValue).getExecutionCommand();
		}
		return this.bodyParser.quotedLongText(
				MapConverter.getTestSideValuesForNonBody(header.getServerValue()));
	}

	private boolean headerToIgnore(Header header) {
		return contentTypeOrAccept(header) || headerOfAbsentType(header);
	}

	private boolean contentTypeOrAccept(Header header) {
		return "Content-Type".equalsIgnoreCase(header.getName())
				|| "Accept".equalsIgnoreCase(header.getName());
	}

	private boolean headerOfAbsentType(Header header) {
		return header.getServerValue() instanceof MatchingStrategy
				&& ((MatchingStrategy) header.getServerValue())
						.getType() == MatchingStrategy.Type.ABSENT;
	}

	@Override
	public boolean accept(SingleContractMetadata metadata) {
		return metadata.getContract().getRequest().getHeaders() != null && !metadata
				.getContract().getRequest().getHeaders().getEntries().isEmpty()
				&& !hasHeaderOnlyContentTypeOrAccept(metadata);
	}

	private boolean hasHeaderOnlyContentTypeOrAccept(SingleContractMetadata metadata) {
		Set<Header> entries = metadata.getContract().getRequest().getHeaders()
				.getEntries();
		long filteredOut = entries.stream().filter(this::headerToIgnore).count();
		return filteredOut == entries.size();
	}

}
