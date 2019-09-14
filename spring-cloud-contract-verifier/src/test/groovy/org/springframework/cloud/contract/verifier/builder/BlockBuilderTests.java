package org.springframework.cloud.contract.verifier.builder;

import org.assertj.core.api.BDDAssertions;
import org.junit.Test;

public class BlockBuilderTests {

	@Test
	public void should_add_ending_if_no_special_char_is_at_the_end() {
		BlockBuilder blockBuilder = blockBuilder();

		blockBuilder.append("foo").addEndingIfNotPresent();

		BDDAssertions.then(blockBuilder.toString()).isEqualTo("foo;");

		blockBuilder = blockBuilder();

		blockBuilder.append("foo\n").addEndingIfNotPresent();
		BDDAssertions.then(blockBuilder.toString()).isEqualTo("foo;\n");

		blockBuilder = blockBuilder();

		blockBuilder.append(
				"DocumentContext parsedJson = JsonPath.parse(contractVerifierObjectMapper.writeValueAsString(response.getPayload()))\n")
				.addEndingIfNotPresent();
		BDDAssertions.then(blockBuilder.toString()).isEqualTo(
				"DocumentContext parsedJson = JsonPath.parse(contractVerifierObjectMapper.writeValueAsString(response.getPayload()));\n");
	}

	@Test
	public void should_not_add_ending_if_no_char_is_at_the_end() {
		BlockBuilder blockBuilder = blockBuilder();

		blockBuilder.append("foo;").addEndingIfNotPresent();
		BDDAssertions.then(blockBuilder.toString()).isEqualTo("foo;");

		blockBuilder = blockBuilder();

		blockBuilder.append("foo {").addEndingIfNotPresent();
		BDDAssertions.then(blockBuilder.toString()).isEqualTo("foo {");

		blockBuilder = blockBuilder();

		blockBuilder.append("foo {\n").addEndingIfNotPresent();
		BDDAssertions.then(blockBuilder.toString()).isEqualTo("foo {\n");

		blockBuilder = blockBuilder();

		blockBuilder.append("foo;\n").addEndingIfNotPresent();
		BDDAssertions.then(blockBuilder.toString()).isEqualTo("foo;\n");
	}

	@Test
	public void should_add_space_if_ends_with_a_text() {
		BlockBuilder blockBuilder = blockBuilder();

		blockBuilder.append("foo").addAtTheEndIfEndsWithAChar(" ");
		BDDAssertions.then(blockBuilder.toString()).isEqualTo("foo ");
	}

	@Test
	public void should_not_add_space_if_does_not_end_with_a_text() {
		BlockBuilder blockBuilder = blockBuilder();

		blockBuilder.append("foo\n").addAtTheEndIfEndsWithAChar(" ");
		BDDAssertions.then(blockBuilder.toString()).isEqualTo("foo\n");
	}

	private BlockBuilder blockBuilder() {
		BlockBuilder blockBuilder = new BlockBuilder("\t");
		blockBuilder.setupLineEnding(";");
		return blockBuilder;
	}

}