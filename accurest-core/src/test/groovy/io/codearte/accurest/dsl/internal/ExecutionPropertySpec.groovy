package io.codearte.accurest.dsl.internal

import spock.lang.Specification

class ExecutionPropertySpec extends Specification {

	def 'should insert passed value in place of $it placeholder'() {
		given:
			String commandToExecute = 'commandToExecute($it)'
			ExecutionProperty executionProperty = new ExecutionProperty(commandToExecute)
		and:
			String valueToInsert = 'someObject.itsValue'
		when:
			String commandWithInsertedValue = executionProperty.insertValue(valueToInsert)
		then:
			'commandToExecute(someObject.itsValue)' == commandWithInsertedValue
	}

}
