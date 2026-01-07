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

package org.springframework.cloud.contract.verifier.builder;

import java.util.Optional;

import org.junit.Test;

import org.springframework.cloud.contract.spec.Contract;
import org.springframework.cloud.contract.spec.internal.BodyMatchers;

import static com.toomuchcoding.jsonassert.JsonAssertion.assertThat;

public class XmlBodyVerificationBuilderTest {

	private static final String xml = "<customer>\r\n" + "      <email>customer@test.com</email>\r\n"
			+ "    </customer>";

	@Test
	public void shouldAddXmlProcessingLines() {
		// Given
		XmlBodyVerificationBuilder builder = new XmlBodyVerificationBuilder(new Contract(), Optional.of(";"));
		BlockBuilder blockBuilder = new BlockBuilder(" ");
		BodyMatchers matchers = new BodyMatchers();
		// When
		builder.addXmlResponseBodyCheck(blockBuilder, xml, matchers, xml, true);
		// Then
		String test = blockBuilder.toString();
		assertThat(test).contains("DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();")
			.contains("builderFactory.setNamespaceAware(true);")
			.contains("DocumentBuilder documentBuilder = builderFactory.newDocumentBuilder();")
			.contains("Document parsedXml = documentBuilder.parse(new InputSource(new StringReader(")
			.contains(xml);
	}

}
