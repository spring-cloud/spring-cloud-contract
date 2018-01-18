package contracts.fraudname

org.springframework.cloud.contract.spec.Contract.make {
	// highest priority
	priority(1)
	request {
		method PUT()
		url '/frauds/name'
		body([
			   name: "fraud"
		])
		headers {
			contentType("application/json")
		}
	}
	response {
		status 200
		body([
				result: "Sorry ${fromRequest().body('$.name')} but you're a fraud"
		])
		headers {
			header(contentType(), "${fromRequest().header(contentType())};charset=UTF-8")
		}
	}
}