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
						contentType("application/json")
					}

				}
			response {
				status 200
				body(
						fraudCheckStatus: "OK",
						"rejection.reason": $(consumer(null), producer(execute('assertThatRejectionReasonIsNull($it)')))
				)
				headers {
					contentType("application/json")
				}
			}

}
