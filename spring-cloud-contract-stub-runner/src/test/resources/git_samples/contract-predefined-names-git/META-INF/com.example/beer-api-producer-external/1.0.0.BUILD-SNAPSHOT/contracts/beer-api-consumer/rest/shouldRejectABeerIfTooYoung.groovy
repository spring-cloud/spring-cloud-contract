package contracts.beer.rest

org.springframework.cloud.contract.spec.Contract.make {
	request {
		description("""
Represents a unsuccessful scenario of getting a beer

given:
	client is not old enough
when:
	he applies for a beer
then:
	we'll NOT grant him the beer
""")
		method 'POST'
		url '/check'
		body(
				age: value(consumer(regex('[0-1][0-9]')))
		)
		headers {
			header 'Content-Type', 'application/json'
		}
	}
	response {
		status 200
		body( """
			{
				"status": "NOT_OK"
			}
			""")
		headers {
			header(
					'Content-Type', value(consumer('application/json'),producer(regex('application/json.*')))
			)
		}
	}
}
