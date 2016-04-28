io.codearte.accurest.dsl.GroovyDsl.make {
	request {
		url '/foo'
		method 'GET'
	}
	response {
		status 200
		body 'bar'
	}
}