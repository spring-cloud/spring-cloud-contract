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

import org.apache.commons.lang3.StringUtils;

/**
 * Builds a block of code. Allows to start, end, indent etc. pieces of code.
 *
 * @author Jakub Kubrynski, codearte.io
 * @since 1.0.0
 */
public class BlockBuilder {

	private final StringBuilder builder;

	private final String spacer;

	private int indents;

	private String lineEnding = "";

	private String labelPrefix = "";

	/**
	 * @param spacer - char used for spacing
	 */
	public BlockBuilder(String spacer) {
		this.spacer = spacer;
		builder = new StringBuilder();
	}

	/**
	 * Setup line ending
	 */
	public BlockBuilder setupLineEnding(String lineEnding) {
		this.lineEnding = lineEnding;
		return this;
	}

	/**
	 * Setup label prefix
	 */
	public BlockBuilder setupLabelPrefix(String labelPrefix) {
		this.labelPrefix = labelPrefix;
		return this;
	}

	public String getLineEnding() {
		return this.lineEnding;
	}

	/**
	 * Adds indents to start a new block
	 */
	public BlockBuilder appendWithLabelPrefix(String label) {
		return append(this.labelPrefix).append(label);
	}

	/**
	 * Adds indents to start a new block
	 */
	public BlockBuilder startBlock() {
		indents++;
		return this;
	}

	/**
	 * Ends block by removing indents
	 */
	public BlockBuilder endBlock() {
		indents--;
		return this;
	}

	/**
	 * Creates a block and adds indents
	 */
	public BlockBuilder indent() {
		startBlock().startBlock();
		return this;
	}

	/**
	 * Removes indents and closes the block
	 */
	public BlockBuilder unindent() {
		endBlock().endBlock();
		return this;
	}

	public BlockBuilder addLine(String line) {
		return addIndented(line).append("\n");
	}

	public BlockBuilder addIndented(String line) {
		return addIndentation().append(line);
	}

	public BlockBuilder addIndented(Runnable runnable) {
		addIndentation();
		runnable.run();
		return this;
	}

	public BlockBuilder addLineWithEnding(String line) {
		addIndentation();
		append(line).addEndingIfNotPresent().addEmptyLine();
		return this;
	}

	public BlockBuilder addEndingIfNotPresent() {
		addAtTheEnd(lineEnding);
		return this;
	}

	public BlockBuilder addEmptyLine() {
		builder.append("\n");
		return this;
	}

	public BlockBuilder appendWithSpace(String text) {
		return addAtTheEnd(" ").append(text);
	}

	public BlockBuilder appendWithSpace(Runnable runnable) {
		addAtTheEnd(" ");
		runnable.run();
		return this;
	}

	public BlockBuilder append(Runnable runnable) {
		runnable.run();
		return this;
	}

	public BlockBuilder append(String string) {
		builder.append(string);
		return this;
	}

	public BlockBuilder addIndentation() {
		for (int i = 0; i < indents; i++) {
			builder.append(spacer);
		}
		return this;
	}

	BlockBuilder inBraces(Runnable runnable) {
		builder.append("{\n");
		startBlock();
		runnable.run();
		endBlock();
		addAtTheEnd("\n");
		addLine("}");
		return this;
	}

	public boolean endsWith(String text) {
		return builder.toString().endsWith(text);
	}

	public BlockBuilder addAtTheEndIfEndsWithAChar(String toAdd) {
		char lastChar = builder.charAt(builder.length() - 1);
		if (Character.isLetter(lastChar)) {
			builder.append(toAdd);
		}
		return this;
	}

	/**
	 * Adds the given text at the end of the line
	 * @return updated BlockBuilder
	 */
	public BlockBuilder addAtTheEnd(String toAdd) {
		String lastChar = String.valueOf(builder.charAt(builder.length() - 1));
		String secondLastChar = builder.length() >= 2 ? String.valueOf(builder.charAt(builder.length() - 2)) : "";
		boolean isEndWithNewLine = endsWithNewLine(lastChar);
		boolean lastCharSpecial = aSpecialSign(lastChar, toAdd);
		boolean secondLastCharSpecial = aSpecialSign(secondLastChar, toAdd);
		boolean lineEndingToAdd = toAdd.equals(lineEnding);
		// lastChar = [;] , toAdd = [;]
		if (lastChar.equals(toAdd)) {
			return this;
		}
		// secondLastChar = [ ], lastChar = [{] , toAdd = [;]
		else if ((!isEndWithNewLine && lastCharSpecial) && lineEndingToAdd) {
			return this;
		}
		// secondLastChar = [{], lastChar = [\n] , toAdd = [;]
		else if (isEndWithNewLine && secondLastCharSpecial) {
			return this;
		}
		else if (isEndWithNewLine && !secondLastCharSpecial) {
			builder.replace(builder.length() - 1, builder.length(), toAdd);
			builder.append("\n");
		}
		else {
			builder.append(toAdd);
		}
		return this;
	}

	private boolean endsWithNewLine(String character) {
		return character.equals("\n");
	}

	private boolean aSpecialSign(String character, String toAdd) {
		if (StringUtils.isEmpty(character)) {
			return false;
		}
		return character.equals("{") || (character.equals(spacer) && toAdd.equals(spacer))
				|| (character.equals(spacer) && toAdd.equals(" ")) || character.equals(toAdd)
				|| (endsWithNewLine(character) && StringUtils.equalsAny(toAdd, "\n", " ", lineEnding));
	}

	/**
	 * Updates the current text with the provided one
	 * @param contents - text to replace the current content with
	 * @return updated Block Builder
	 */
	public BlockBuilder updateContents(String contents) {
		this.builder.replace(0, this.builder.length(), contents);
		return this;
	}

	@Override
	public String toString() {
		return builder.toString();
	}

}
