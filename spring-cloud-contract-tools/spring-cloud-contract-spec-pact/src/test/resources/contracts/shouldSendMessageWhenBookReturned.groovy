package contracts

import org.springframework.cloud.contract.spec.Contract

[
		Contract.make {
			label 'some_label'
			input {
				triggeredBy('bookReturnedTriggered()')
			}
			outputMessage {
				sentTo('activemq:output')
				body('''{ "bookName" : "foo" }''')
				headers {
					header('BOOK-NAME', 'foo')
					messagingContentType(applicationJson())
				}
			}
		}
]