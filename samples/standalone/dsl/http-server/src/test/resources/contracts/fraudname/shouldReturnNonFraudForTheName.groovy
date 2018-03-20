package contracts.fraudname

org.springframework.cloud.contract.spec.Contract.make {
	request {
		method PUT()
		url '/frauds/name'
		body([
			   name: $(anyAlphaUnicode())
		])
		headers {
			contentType("application/json")
		}
	}
	response {
		status OK()
		body([
				result: "Don't worry ${fromRequest().body('$.name')} you're not a fraud"
		])
		headers {
			header(contentType(), "${fromRequest().header(contentType())};charset=UTF-8")
		}
	}
}