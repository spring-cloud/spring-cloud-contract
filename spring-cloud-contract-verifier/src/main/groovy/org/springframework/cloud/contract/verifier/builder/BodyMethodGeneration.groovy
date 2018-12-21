package org.springframework.cloud.contract.verifier.builder

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
}
