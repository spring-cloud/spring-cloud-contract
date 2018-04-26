package org.springframework.cloud.contract.verifier.spec.kotlin

class ExecutionProperty(val executionCommand: String) : java.io.Serializable {

    val PLACEHOLDER_VALUE = "#it"

    /**
     * Inserts the provided code as a parameter to the method and returns
     * the code that represents that method execution
     */
    fun insertValue(valueToInsert: String) = executionCommand.replace(PLACEHOLDER_VALUE, valueToInsert)

    override fun toString() = executionCommand
}