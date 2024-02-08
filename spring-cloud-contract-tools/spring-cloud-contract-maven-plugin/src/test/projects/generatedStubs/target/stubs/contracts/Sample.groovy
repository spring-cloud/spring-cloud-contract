io.codearte.accurest.dsl.GroovyDsl.make {
	request {
		method 'POST'
		url('/users') {

		}
		headers {
			header 'Content-Type': 'application/json'
		}
		body '''{ "login" : "john", "name": "John The Contract" }'''
	}
	response {
		status OK()
		headers {
			header 'Location': '/users/john'
		}
	}
}
