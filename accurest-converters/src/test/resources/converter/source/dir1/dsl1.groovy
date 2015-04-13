io.codearte.accurest.dsl.GroovyDsl.make {
	request {
		method('PUT')
		headers {
			header 'Content-Type': 'application/json'
		}
		urlPattern $(client('/[0-9]{2}'), server('/12'))
	}
	response {
		status 200
	}
}
