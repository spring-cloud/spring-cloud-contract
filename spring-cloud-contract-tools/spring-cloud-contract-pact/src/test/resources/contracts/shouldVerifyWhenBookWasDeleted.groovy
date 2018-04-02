package contracts

import org.springframework.cloud.contract.spec.Contract

[
		Contract.make {
			label 'some_label'
			input {
				messageFrom('jms:delete')
				messageBody([
						bookName: 'foo'
				])
				messageHeaders {
					header('sample', 'header')
				}
				assertThat('bookWasDeleted()')
			}
		}
]