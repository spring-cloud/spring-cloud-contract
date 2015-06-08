package io.codearte.accurest.dsl.internal

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import groovy.transform.TypeChecked

@EqualsAndHashCode(includeFields = true)
@ToString(includePackage = false, includeFields = true, ignoreNulls = true, includeNames = true)
@TypeChecked
class QueryParameters  {

    List<QueryParameter> parameters = []

    void parameter(Map<String, Object> singleParameter) {
        Map.Entry<String, Object> first = singleParameter.entrySet().first()
        parameters << new QueryParameter(first?.key, first?.value)
    }

    void parameter(String parameterName, Object parameterValue) {
        parameters << new QueryParameter(parameterName, parameterValue)
    }

    MatchingStrategy equalTo(Object value) {
        return new MatchingStrategy(value, MatchingStrategy.Type.EQUAL_TO)
    }

    MatchingStrategy containing(Object value) {
        return new MatchingStrategy(value, MatchingStrategy.Type.CONTAINS)
    }

    MatchingStrategy matching(Object value) {
        return new MatchingStrategy(value, MatchingStrategy.Type.MATCHING)
    }

    MatchingStrategy notMatching(Object value) {
        return new MatchingStrategy(value, MatchingStrategy.Type.NOT_MATCHING)
    }

}
