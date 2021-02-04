org.springframework.cloud.contract.spec.Contract.make {
	label 'return_book_3'
	input {
		messageFrom('input2')
		messageBody([
				bookName: 'bar'
		])
		messageHeaders {
			header('kafka_receivedMessageKey', 'bar5150')
		}
	}
	outputMessage {
		sentTo('output')
		body([
				bookName: 'bar'
		])
		headers {
			header('BOOK-NAME', 'bar')
			header('kafka_messageKey', 'bar5150')
		}
	}
}