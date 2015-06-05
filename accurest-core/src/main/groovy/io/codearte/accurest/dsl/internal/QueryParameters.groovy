package io.codearte.accurest.dsl.internal

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import groovy.transform.TypeChecked

@EqualsAndHashCode(includeFields = true)
@ToString(includePackage = false, includeFields = true, ignoreNulls = true, includeNames = true)
@TypeChecked
public class QueryParameters  {

    List<QueryParameter> parameters = []

    public void parameter(Map<String, Object> singleParameter) {
        Map.Entry<String, Object> first = singleParameter.entrySet().first()
        parameters << new QueryParameter(first?.key, first?.value)
    }

    public void parameter(String parameterName, Object parameterValue) {
        parameters << new QueryParameter(parameterName, parameterValue)
    }

    def equalTo(Object value) {
        return new MatchingStrategy(value, MatchingStrategy.Type.EQUAL_TO)
    }

    def containing(Object value) {
        return new MatchingStrategy(value, MatchingStrategy.Type.CONTAINS)
    }

    def matching(Object value) {
        return new MatchingStrategy(value, MatchingStrategy.Type.MATCHING)
    }

    def notMatching(Object value) {
        return new MatchingStrategy(value, MatchingStrategy.Type.NOT_MATCHING)
    }

    void collect(Closure closure) {
        parameters?.each {
            parameter -> closure(parameter)
        }
    }

}
