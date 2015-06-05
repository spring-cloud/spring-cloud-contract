package io.codearte.accurest.dsl.internal

import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString;

@EqualsAndHashCode(includeFields = true)
@ToString(includePackage = false, includeFields = true, ignoreNulls = true, includeNames = true)
@CompileStatic
public class MatchingStrategy extends DslProperty {

    Type type

    MatchingStrategy(Object value, Type type) {
        super(value)
        this.type = type
    }

    MatchingStrategy(DslProperty value, Type type) {
        super(value.clientValue, value.serverValue)
        this.type = type
    }

    enum Type {
        EQUAL_TO("equalTo"), CONTAINS("contains"), MATCHING("matches"), NOT_MATCHING("doesNotMatch")

        final String name

        Type(name) {
            this.name = name
        }
    }

}