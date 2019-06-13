/*
 * Copyright 2013-2019 the original author or authors.
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

package org.springframework.cloud.contract.verifier.builder


import groovy.transform.CompileStatic
import groovy.transform.PackageScope
/**
 * Builds a block of code. Allows to start, end, indent etc. pieces of code.
 *
 * @author Jakub Kubrynski, codearte.io
 *
 * @since 1.0.0
 */
@CompileStatic
class BlockBuilder {

	private final StringBuilder builder
	private final String spacer
	private int indents
	private String lineEnding = ""

	/**
	 * @param spacer - char used for spacing
	 */
	BlockBuilder(String spacer) {
		this.spacer = spacer
		builder = new StringBuilder()
	}

	/**
	 * Setup line ending
	 */
	BlockBuilder lineEnding(String lineEnding) {
		this.lineEnding = lineEnding
		return this
	}


	String getLineEnding() {
		return this.lineEnding
	}

	/**
	 * Adds indents to start a new block
	 */
	BlockBuilder startBlock() {
		indents++
		return this
	}

	/**
	 * Ends block by removing indents
	 */
	BlockBuilder endBlock() {
		indents--
		return this
	}

	/**
	 * Creates a block and adds indents
	 */
	BlockBuilder indent() {
		startBlock().startBlock()
		return this
	}

	/**
	 * Removes indents and closes the block
	 */
	BlockBuilder unindent() {
		endBlock().endBlock()
		return this
	}

	BlockBuilder addLine(String line) {
		return addIndented(line).append("\n")
	}

	BlockBuilder addIndented(String line) {
		addIndentation()
		builder << line
		return this
	}

	BlockBuilder addLineWithEnding(String line) {
		addIndentation()
		append(line).addEndingIfNotPresent().addEmptyLine()
		return this
	}

	BlockBuilder addEnding() {
		builder << lineEnding
		return this
	}

	BlockBuilder addEndingIfNotPresent() {
		addAtTheEnd(lineEnding)
		return this
	}

	BlockBuilder addEmptyLine() {
		builder << '\n'
		return this
	}

	BlockBuilder appendWithSpace(String text) {
		builder << " " + text
		return this
	}

	BlockBuilder appendWithSpace(Runnable runnable) {
		builder << " "
		runnable.run()
		return this
	}

	// synactic sugar
	BlockBuilder append(Runnable runnable) {
		runnable.run()
		return this
	}

	BlockBuilder append(String string) {
		builder << string
		return this
	}

	void addIndentation() {
		indents.times {
			builder << spacer
		}
	}

	@PackageScope
	BlockBuilder addBlock(MethodBuilder methodBuilder) {
		startBlock()
		methodBuilder.appendTo(this)
		endBlock()
		addEmptyLine()
		return this
	}

	@PackageScope
	BlockBuilder inBraces(Runnable runnable) {
		builder.append("{\n")
		startBlock()
		runnable.run()
		endBlock()
		addAtTheEnd('\n')
		addLine("}")
		return this
	}

	boolean endsWith(String text) {
		return builder.toString().endsWith(text)
	}

	/**
	 * Adds the given text at the end of the line
	 *
	 * @return updated BlockBuilder
	 */
	BlockBuilder addAtTheEnd(String toAdd) {
		String lastChar = builder.charAt(builder.length() - 1) as String
		String secondLastChar = builder.length() >= 2 ? builder.
				charAt(builder.length() - 2) as String : ""
		boolean isEndWithNewLine = endsWithNewLine(lastChar)
		boolean lastCharSpecial = aSpecialSign(lastChar, toAdd)
		boolean secondLastCharSpecial = aSpecialSign(secondLastChar, toAdd)
		if (isEndWithNewLine && secondLastCharSpecial || lastCharSpecial) {
			return this
		}
		else if (lastChar == toAdd) {
			return this
		}
		else if (endsWithNewLine(lastChar) && !aSpecialSign(secondLastChar, toAdd)) {
			builder.replace(builder.length() - 1, builder.length(), toAdd)
			builder << '\n'
		}
		else {
			builder << toAdd
		}
		return this
	}

	private boolean endsWithNewLine(String character) {
		return character as String == '\n'
	}

	private boolean aSpecialSign(String character, String toAdd) {
		if (!character) {
			return false
		}
		return character == "{" || (character == spacer && toAdd == spacer) || character == toAdd || (endsWithNewLine(character) && toAdd == '\n')
	}

	/**
	 * Updates the current text with the provided one
	 *
	 * @param contents - text to replace the current content with
	 * @return updated Block Builder
	 */
	BlockBuilder updateContents(String contents) {
		this.builder.replace(0, this.builder.length(), contents)
		return this
	}

	@Override
	String toString() {
		return builder.toString()
	}
}
