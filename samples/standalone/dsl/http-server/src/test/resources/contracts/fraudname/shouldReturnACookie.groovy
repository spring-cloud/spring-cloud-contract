package contracts.fraudname

org.springframework.cloud.contract.spec.Contract.make {
	request {
		method GET()
		url '/frauds/name'
		cookies {
			cookie("name", "foo")
			cookie(name2: "bar")
		}
	}
	response {
		status 200
		body("foo bar")
	}
}