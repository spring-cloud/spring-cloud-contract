package org.springframework.cloud.contract.verifier.builder

import org.apache.commons.text.StringEscapeUtils

import org.springframework.cloud.contract.spec.internal.BodyMatcher
import org.springframework.cloud.contract.spec.internal.MatchingType
import org.springframework.util.SerializationUtils

/**
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
