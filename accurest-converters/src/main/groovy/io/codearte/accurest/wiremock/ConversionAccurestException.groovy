package io.codearte.accurest.wiremock

import groovy.transform.CompileStatic
import io.coderate.accurest.AccurestException

@CompileStatic
class ConversionAccurestException extends AccurestException {

	ConversionAccurestException(String message, Throwable cause) {
		super(message, cause)
	}
}
