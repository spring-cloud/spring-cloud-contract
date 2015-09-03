io.codearte.accurest.dsl.GroovyDsl.make {
	request {
		method('PUT')
		headers {
			header 'Content-Type': 'application/json'
		}
		body("""\
		  {
			"name": "Jan",
			"id": "${value(client('abc'), server('def'))}",
		  }
		  """
		)
		url $(client('/[0-9]{2}'), server('/12'))
	}
	response {
		status 200
		body("""\
		  {
			"name": "Jan",
			"id": "${value(client('123'), server('321'))}",
						"surname": "${value(client('Kowalsky'), server('$checkIfSurnameValid($value)'))}"
		  }
		  """
		)
		headers {
			header 'Content-Type': 'text/plain'
		}
	}
}
