package io.coderate.accurest

/**
 * @author Jakub Kubrynski
 */
class AccurestException extends RuntimeException {

	AccurestException(String message) {
		super(message)
	}

	AccurestException(String message, Throwable cause) {
		super(message, cause)
	}
}
