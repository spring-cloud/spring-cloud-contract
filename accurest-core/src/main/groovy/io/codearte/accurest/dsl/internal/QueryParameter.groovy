package io.codearte.accurest.dsl.internal;

import groovy.transform.CompileStatic;
import groovy.transform.EqualsAndHashCode;
import groovy.transform.ToString

import static io.codearte.accurest.util.ValidateUtils.validateServerValueIsAvailable;

@EqualsAndHashCode(includeFields = true)
@ToString(includePackage = false, includeFields = true, ignoreNulls = true, includeNames = true)
@CompileStatic
class QueryParameter extends DslProperty {

    String name

    QueryParameter(String name, DslProperty dslProperty) {
        super(dslProperty.clientValue, dslProperty.serverValue)
        validateServerValueIsAvailable(dslProperty.serverValue, "Query parameter '$name'")
        this.name = name
    }

    QueryParameter(String name, MatchingStrategy matchingStrategy) {
        super(matchingStrategy)
        validateServerValueIsAvailable(matchingStrategy, "Query parameter '$name'")
        this.name = name
    }

    QueryParameter(String name, Object value) {
        super(value)
        validateServerValueIsAvailable(value, "Query parameter '$name'")
        this.name = name
    }

}
