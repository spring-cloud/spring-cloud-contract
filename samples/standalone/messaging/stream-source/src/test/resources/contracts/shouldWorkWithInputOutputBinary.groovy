package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
	label("positive")
	input {
		messageFrom("bytes_input")
		messageBody(fileAsBytes("input.pdf"))
		messageHeaders {
			messagingContentType(applicationOctetStream())
		}
	}
	outputMessage {
		sentTo("bytes_output")
		body(fileAsBytes("output.pdf"))
		headers {
			messagingContentType(applicationOctetStream())
		}
	}
}
