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

import org.springframework.cloud.contract.spec.internal.Cookie;
import org.springframework.cloud.contract.spec.internal.MatchingStrategy;
import org.springframework.cloud.contract.spec.internal.Request;
import org.springframework.cloud.contract.verifier.file.SingleContractMetadata;

class MockMvcCookiesGiven implements Given {

	private final BlockBuilder blockBuilder;

	MockMvcCookiesGiven(BlockBuilder blockBuilder) {
		this.blockBuilder = blockBuilder;
	}

	@Override
	public MethodVisitor<Given> apply(SingleContractMetadata metadata) {
		processInput(metadata.getContract().getRequest());
		return this;
	}

	private void processInput(Request request) {
		Iterator<Cookie> iterator = request.getCookies().getEntries().iterator();
		while (iterator.hasNext()) {
			Cookie cookie = iterator.next();
			if (ofAbsentType(cookie)) {
				return;
			}
			if (iterator.hasNext()) {
				this.blockBuilder.addLine(string(cookie));
			}
			else {
				this.blockBuilder.addIndented(string(cookie));
			}
		}
	}

	private String string(Cookie cookie) {
		return ".cookie(" + ContentHelper.getTestSideForNonBodyValue(cookie.getKey())
				+ ", " + ContentHelper.getTestSideForNonBodyValue(cookie.getServerValue())
				+ ")";
	}

	private boolean ofAbsentType(Cookie cookie) {
		return cookie.getServerValue() instanceof MatchingStrategy
				&& MatchingStrategy.Type.ABSENT
						.equals(((MatchingStrategy) cookie.getServerValue()).getType());
	}

	@Override
	public boolean accept(SingleContractMetadata metadata) {
		Request request = metadata.getContract().getRequest();
		return request != null && request.getCookies() != null;
	}

}
