package io.codearte.accurest.util

import groovy.transform.TypeChecked
import io.codearte.accurest.dsl.internal.DslProperty
import io.codearte.accurest.dsl.internal.MatchingStrategy

import java.util.regex.Pattern

@TypeChecked
class ValidateUtils {

    static Object validateServerValueIsAvailable(Object value) {
        validateServerValueIsAvailable(value, "Server value")
        return value
    }

    static Object validateServerValueIsAvailable(Object value, String msg) {
        validateServerValue(value, msg)
        return value
    }

    static void validateServerValue(Pattern pattern, String msg) {
        throw new IllegalStateException("$msg can't be a pattern for the server side")
    }

    static void validateServerValue(MatchingStrategy matchingStrategy, String msg) {
        if (matchingStrategy.type != MatchingStrategy.Type.EQUAL_TO) {
            throw new IllegalStateException("$msg can't be of a matching type: $matchingStrategy.type for the server side")
        }
        validateServerValue(matchingStrategy.serverValue, msg)
    }

    static void validateServerValue(DslProperty value, String msg) {
        validateServerValue(value.serverValue, msg)
    }

    static void validateServerValue(Object value, String msg) {
        // OK
    }

}
