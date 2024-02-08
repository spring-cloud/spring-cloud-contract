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

import org.springframework.cloud.contract.spec.internal.Cookie;
import org.springframework.cloud.contract.spec.internal.MatchingStrategy;
import org.springframework.cloud.contract.spec.internal.Request;
import org.springframework.cloud.contract.verifier.file.SingleContractMetadata;
import org.springframework.cloud.contract.verifier.util.MapConverter;

class JaxRsRequestCookiesWhen implements When {

	private final BlockBuilder blockBuilder;

	private final BodyParser bodyParser;

	JaxRsRequestCookiesWhen(BlockBuilder blockBuilder, BodyParser bodyParser) {
		this.blockBuilder = blockBuilder;
		this.bodyParser = bodyParser;
	}

	@Override
	public MethodVisitor<When> apply(SingleContractMetadata metadata) {
		appendCookies(metadata.getContract().getRequest());
		return this;
	}

	private void appendCookies(Request request) {
		Iterator<Cookie> iterator = request.getCookies().getEntries().stream()
				.filter(cookie -> !cookieOfAbsentType(cookie)).iterator();
		while (iterator.hasNext()) {
			Cookie cookie = iterator.next();
			String value = ".cookie(" + this.bodyParser.quotedShortText(cookie.getKey()) + ", "
					+ this.bodyParser.quotedShortText(MapConverter.getTestSideValuesForNonBody(cookie.getServerValue()))
					+ ")";
			if (iterator.hasNext()) {
				this.blockBuilder.addLine(value);
			}
			else {
				this.blockBuilder.addIndented(value);
			}
		}
	}

	private boolean cookieOfAbsentType(Cookie cookie) {
		return cookie.getServerValue() instanceof MatchingStrategy
				&& ((MatchingStrategy) cookie.getServerValue()).getType() == MatchingStrategy.Type.ABSENT;
	}

	@Override
	public boolean accept(SingleContractMetadata metadata) {
		return metadata.getContract().getRequest().getCookies() != null
				&& !metadata.getContract().getRequest().getCookies().getEntries().isEmpty()
				&& !hasOnlyAbsentCookies(metadata);
	}

	private boolean hasOnlyAbsentCookies(SingleContractMetadata metadata) {
		Set<Cookie> entries = metadata.getContract().getRequest().getCookies().getEntries();
		long filteredOut = entries.stream().filter(this::cookieOfAbsentType).count();
		return filteredOut == entries.size();
	}

}
