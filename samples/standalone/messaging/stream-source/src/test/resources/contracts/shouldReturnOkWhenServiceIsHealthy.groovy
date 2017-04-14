package contracts

org.springframework.cloud.contract.spec.Contract.make {
	request {
		method(GET())
		url("/health")
	}
	response {
		status(200)
	}
}
