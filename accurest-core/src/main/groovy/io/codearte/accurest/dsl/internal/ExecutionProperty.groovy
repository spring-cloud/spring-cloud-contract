package io.codearte.accurest.dsl.internal

import groovy.transform.CompileStatic

@CompileStatic
class ExecutionProperty {

    private static final String PLACEHOLDER_VALUE = '\\$it'

    final String executionCommand

    ExecutionProperty(String executionCommand) {
        this.executionCommand = executionCommand
    }

    String insertValue(String valueToInsert) {
        return executionCommand.replaceAll(PLACEHOLDER_VALUE, valueToInsert)
    }
}
