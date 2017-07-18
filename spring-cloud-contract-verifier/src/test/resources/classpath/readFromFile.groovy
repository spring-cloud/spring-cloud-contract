import org.springframework.cloud.contract.spec.Contract

Contract.make {
	request {
		method('PUT')
		headers {
			contentType(applicationJson())
		}
		body(file("request.json"))
		url("/1")
	}
	response {
		status 200
		body(file("response.json"))
		headers {
			contentType(textPlain())
		}
	}
}
