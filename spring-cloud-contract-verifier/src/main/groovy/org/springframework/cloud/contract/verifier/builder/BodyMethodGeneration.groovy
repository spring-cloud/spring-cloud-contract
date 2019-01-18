/*
 *  Copyright 2013-2019 the original author or authors.
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

import org.apache.commons.text.StringEscapeUtils

import org.springframework.cloud.contract.spec.internal.BodyMatcher
import org.springframework.cloud.contract.spec.internal.MatchingType
import org.springframework.util.SerializationUtils

/**
 * @author Marcin Grzejszczak
 * @author Olga Maciaszek-Sharma
 * @since 2.1.0
 */
trait BodyMethodGeneration {

	// Doing a clone doesn't work for nested lists...
	Object cloneBody(Object object) {
		if (object instanceof List || object instanceof Map) {
			byte[] serializedObject = SerializationUtils.serialize(object)
			return SerializationUtils.deserialize(serializedObject)
		}
		try {
			return object.clone()
		}
		catch (CloneNotSupportedException ignored) {
			return object
		}
	}

	void addColonIfRequired(Optional<String> lineSuffix, BlockBuilder blockBuilder) {
		lineSuffix.ifPresent({
			blockBuilder.addAtTheEnd(lineSuffix.get())
		})
	}

	void addBodyMatchingBlock(List<BodyMatcher> matchers, BlockBuilder blockBuilder,
							  Object responseBody, boolean shouldCommentOutBDDBlocks) {
		blockBuilder.endBlock()
		blockBuilder.addLine(getAssertionJoiner(shouldCommentOutBDDBlocks))
		blockBuilder.startBlock()
		matchers.each {
			if (it.matchingType() == MatchingType.NULL) {
				methodForNullCheck(it, blockBuilder)
			}
			else if (MatchingType.regexRelated(it.matchingType()) || it
					.matchingType() == MatchingType.EQUALITY) {
				methodForEqualityCheck(it, blockBuilder, responseBody)
			}
			else if (it.matchingType() == MatchingType.COMMAND) {
				methodForCommandExecution(it, blockBuilder, responseBody)
			}
			else {
				methodForTypeCheck(it, blockBuilder, responseBody)
			}
		}
	}

	String quotedAndEscaped(String string) {
		return '"' + StringEscapeUtils.escapeJava(string) + '"'
	}

	abstract void methodForNullCheck(BodyMatcher bodyMatcher, BlockBuilder bb)

	abstract void methodForEqualityCheck(BodyMatcher bodyMatcher, BlockBuilder bb, Object body)

	abstract void methodForCommandExecution(BodyMatcher bodyMatcher, BlockBuilder bb, Object body)

	abstract void methodForTypeCheck(BodyMatcher bodyMatcher, BlockBuilder bb, Object body)

	String getAssertionJoiner(boolean shouldCommentOutBDDBlocks) {
		return shouldCommentOutBDDBlocks ? '// and:' : 'and:'
	}
}
