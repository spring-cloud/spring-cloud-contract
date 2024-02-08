import org.springframework.cloud.contract.spec.Contract

Contract.make {
	request {
		url("/1")
		method(PUT())
		headers {
			contentType(applicationOctetStream())
		}
		body(fileAsBytes("request.pdf"))
	}
	response {
		status 200
		body(fileAsBytes("response.pdf"))
		headers {
			contentType(applicationOctetStream())
		}
	}
}
