/*
 * Copyright 2013-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.contract.spec.internal

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

	def 'should insert passed value with a $ sign in place of $it placeholder'() {
		given:
			String commandToExecute = 'commandToExecute($it)'
			ExecutionProperty executionProperty = new ExecutionProperty(commandToExecute)
		and:
			String valueToInsert = '$.someObject.itsValue'
		when:
			String commandWithInsertedValue = executionProperty.insertValue(valueToInsert)
		then:
			'commandToExecute($.someObject.itsValue)' == commandWithInsertedValue
	}

}
