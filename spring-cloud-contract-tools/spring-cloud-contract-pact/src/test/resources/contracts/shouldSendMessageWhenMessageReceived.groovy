package contracts

import org.springframework.cloud.contract.spec.Contract

[
		Contract.make {
			label 'some_label'
			input {
				messageFrom('jms:input')
				messageBody([
						bookName: 'foo'
				])
				messageHeaders {
					header('sample', 'header')
				}
			}
			outputMessage {
				sentTo('jms:output')
				body([
						bookName: 'foo'
				])
				headers {
					header('BOOK-NAME', 'foo')
				}
			}
		}
]