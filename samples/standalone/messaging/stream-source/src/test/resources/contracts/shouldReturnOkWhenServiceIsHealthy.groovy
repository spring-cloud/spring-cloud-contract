package contracts

org.springframework.cloud.contract.spec.Contract.make {
	request {
		method(GET())
		url("/foo")
	}
	response {
		status(200)
	}
}
