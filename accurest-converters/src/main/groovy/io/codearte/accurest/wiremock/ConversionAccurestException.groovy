package io.codearte.accurest.wiremock

import groovy.transform.CompileStatic

@CompileStatic
class ConversionAccurestException extends RuntimeException {

    ConversionAccurestException(String message, Throwable cause) {
        super(message, cause)
    }
}
