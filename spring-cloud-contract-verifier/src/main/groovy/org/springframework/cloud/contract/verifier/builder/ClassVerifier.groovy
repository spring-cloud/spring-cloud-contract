package org.springframework.cloud.contract.verifier.builder

/**
 * @author Olga Maciaszek-Sharma
 * @since 2.1.0
 */
trait ClassVerifier {

	Class classToCheck(Object elementFromBody) {
		switch (elementFromBody.getClass()) {
			case List:
				return List
			case Set:
				return Set
			case Map:
				return Map
			default:
				return elementFromBody.class
		}
	}
}
