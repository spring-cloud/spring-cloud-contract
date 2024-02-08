org.springframework.cloud.contract.spec.Contract.make {
	label 'return_book_1'
	input {
		triggeredBy('bookReturnedTriggered()')
	}
	outputMessage {
		sentTo('output')
		body('''{ "bookName" : "foo" }''')
		headers {
			header('BOOK-NAME', 'foo')
		}
	}
}