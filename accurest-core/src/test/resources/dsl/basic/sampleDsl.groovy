io.coderate.accurest.dsl.GroovyDsl.make {
	request {
		method('PUT')
		headers {
			header 'Content-Type': 'application/json'
		}
		body("""\
          {
            "name": "Jan"
          }
          """
		)
		urlPattern $(client('/[0-9]{2}'), server('/12'))
	}
	response {
		status 200
		body("""\
          {
            "name": "Jan"
          }
          """
				)
		headers {
			header 'Content-Type': 'text/plain'
		}
	}
}
