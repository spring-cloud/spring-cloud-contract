package contracts.foo.bar.bazService.bazConsumer.rest

org.springframework.cloud.contract.spec.Contract.make {
	request {
		method 'GET'
		url '/hello'
	}
	response {
		status 200
	}
}
