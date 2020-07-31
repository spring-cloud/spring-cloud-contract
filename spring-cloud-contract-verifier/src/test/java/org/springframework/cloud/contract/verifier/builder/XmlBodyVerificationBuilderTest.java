package org.springframework.cloud.contract.verifier.builder;

import org.junit.Test;
import org.springframework.cloud.contract.spec.Contract;
import org.springframework.cloud.contract.spec.internal.BodyMatchers;

import java.util.Optional;

import static com.toomuchcoding.jsonassert.JsonAssertion.assertThat;

public class XmlBodyVerificationBuilderTest {

	private static final String xml = "<customer>\r\n"
			+ "      <email>customer@test.com</email>\r\n" + "    </customer>";

	@Test
	public void shouldAddXmlProcessingLines() {
		// Given
		XmlBodyVerificationBuilder builder = new XmlBodyVerificationBuilder(
				new Contract(), Optional.of(";"));
		BlockBuilder blockBuilder = new BlockBuilder(" ");
		BodyMatchers matchers = new BodyMatchers();
		// When
		builder.addXmlResponseBodyCheck(blockBuilder, xml, matchers, xml, true);
		// Then
		String test = blockBuilder.toString();
		assertThat(test).contains(
				"DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();")
				.contains("builderFactory.setNamespaceAware(true);")
				.contains(
						"DocumentBuilder documentBuilder = builderFactory.newDocumentBuilder();")
				.contains(
						"Document parsedXml = documentBuilder.parse(new InputSource(new StringReader(")
				.contains(xml);
	}

}