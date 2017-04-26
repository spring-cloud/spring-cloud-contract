package contracts

org.springframework.cloud.contract.spec.Contract.make {
				request {
					method 'PUT'
					url '/fraudcheck'
					body("""
						{
						"client.id":"${value(consumer(regex('[0-9]{10}')), producer('1234567890'))}",
						"loanAmount":123.123
						}
					"""
					)
					headers {
						header('Content-Type', 'application/vnd.fraud.v1+json')
					}

				}
			response {
				status 200
				body(
						fraudCheckStatus: "OK",
						"rejection.reason": $(consumer(null), producer(execute('assertThatRejectionReasonIsNull($it)')))
				)
				headers {
					header('Content-Type': value(
							producer(regex('application/vnd.fraud.v1.json.*')),
							consumer('application/vnd.fraud.v1+json'))
					)
				}
			}

}
