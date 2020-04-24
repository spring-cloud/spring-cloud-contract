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
	private String labelPrefix = ""

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
	BlockBuilder setupLineEnding(String lineEnding) {
		this.lineEnding = lineEnding
		return this
	}

	/**
	 * Setup label prefix
	 */
	BlockBuilder setupLabelPrefix(String labelPrefix) {
		this.labelPrefix = labelPrefix
		return this
	}


	String getLineEnding() {
		return this.lineEnding
	}

	/**
	 * Adds indents to start a new block
	 */
	BlockBuilder appendWithLabelPrefix(String label) {
		return append(this.labelPrefix).append(label)
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
		return addIndentation().append(line)
	}

	BlockBuilder addIndented(Runnable runnable) {
		addIndentation()
		runnable.run()
		return this
	}

	BlockBuilder addLineWithEnding(String line) {
		addIndentation()
		append(line).addEndingIfNotPresent().addEmptyLine()
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
		return addAtTheEnd(" ").append(text)
	}

	BlockBuilder appendWithSpace(Runnable runnable) {
		addAtTheEnd(" ")
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

	BlockBuilder addIndentation() {
		indents.times {
			builder << spacer
		}
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

	BlockBuilder addAtTheEndIfEndsWithAChar(String toAdd) {
		char lastChar = builder.charAt(builder.length() - 1)
		if (Character.isLetter(lastChar)) {
			builder.append(toAdd)
		}
		return this
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
		boolean lineEndingToAdd = toAdd == lineEnding
		// lastChar = [;] , toAdd = [;]
		if (lastChar == toAdd) {
			return this
		}
		// secondLastChar = [ ], lastChar = [{] , toAdd = [;]
		else if ((!isEndWithNewLine && lastCharSpecial) && lineEndingToAdd) {
			return this
		}
		// secondLastChar = [{], lastChar = [\n] , toAdd = [;]
		else if (isEndWithNewLine && secondLastCharSpecial) {
			return this
		}
		else if (isEndWithNewLine && !secondLastCharSpecial) {
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
		return character == "{" ||
				(character == spacer && toAdd == spacer) ||
				(character == spacer && toAdd == " ") ||
				character == toAdd ||
				(endsWithNewLine(character) &&
						(toAdd == '\n' || toAdd == " " || toAdd == lineEnding))
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
