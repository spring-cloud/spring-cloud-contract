package io.codearte.accurest.util

import io.codearte.accurest.dsl.internal.DslProperty
import io.codearte.accurest.dsl.internal.MatchingStrategy

import java.util.regex.Pattern

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
        throw new IllegalStateException("$msg can't be a pattern")
    }

    static void validateServerValue(MatchingStrategy matchingStrategy, String msg) {
        if (matchingStrategy.type != MatchingStrategy.Type.EQUAL_TO) {
            throw new IllegalStateException("$msg can't be of matching type: $matchingStrategy.type")
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
