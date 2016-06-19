/*
 *  Copyright 2013-2016 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.springframework.cloud.contract.verifier.builder

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.transform.PackageScope

/**
 * Builds a block of code. Allows to start, end, indent etc. pieces of code.
 *
 * @author Jakub Kubrynski, codearte.io
 *
 * @since 1.0.0
 */
@PackageScope
@CompileStatic
class BlockBuilder {

	private final StringBuilder builder
	private final String spacer
	private int indents

	/**
	 * @param spacer - char used for spacing
	 */
	BlockBuilder(String spacer) {
		this.spacer = spacer
		builder = new StringBuilder()
	}

	BlockBuilder startBlock() {
		indents++
		return this
	}

	BlockBuilder endBlock() {
		indents--
		return this
	}

	BlockBuilder indent() {
		startBlock().startBlock()
		return this
	}

	BlockBuilder unindent() {
		endBlock().endBlock()
		return this
	}

	BlockBuilder addLine(String line) {
		addIndentation()
		builder << "$line\n"
		return this
	}

	BlockBuilder addEmptyLine() {
		builder << '\n'
		return this
	}

	@CompileDynamic
	private void addIndentation() {
		indents.times {
			builder << spacer
		}
	}

	BlockBuilder addBlock(MethodBuilder methodBuilder) {
		startBlock()
		methodBuilder.appendTo(this)
		endBlock()
		addEmptyLine()
		return this
	}

	BlockBuilder addAtTheEnd(String toAdd) {
		if (builder.charAt(builder.length() - 1) as String == '\n') {
			builder.replace(builder.length() - 1, builder.length(), toAdd)
			builder << '\n'
		} else {
			builder << toAdd
		}
		return this
	}

	@Override
	String toString() {
		return builder.toString()
	}
}
