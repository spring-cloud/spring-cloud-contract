import org.springframework.cloud.contract.spec.Contract

Contract.make {
	description "should return person by id=1"

	request {
		url "/person/1"
		method GET()
	}

	response {
		status OK()
		headers {
			contentType applicationJson()
		}
		body (
			id: 1,
			name: "foo",
			surname: "bee"
		)
	}
}