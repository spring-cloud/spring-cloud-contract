package contracts

org.springframework.cloud.contract.spec.Contract.make {
			description('''
			The following contract describes a situation where the user should be accepted in our system.
			It satisfies the ticket number ABC-123. For further information please contact the Product Owners
			John Doe and Jane Doe.

			Given:
				a user who wants to borrow an acceptable amount of money
			When:
				he is checked in the fraud system
			Then:
				the fraud check status should be "OK"
			And:
				the rejection reason should not contain any data
			''')
				request {
					method 'PUT'
					url '/fraudcheck'
					body("""
						{
						"clientId":"${value(consumer(regex('[0-9]{10}')), producer('1234567890'))}",
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
						rejectionReason: $(consumer(null), producer(execute('assertThatRejectionReasonIsNull($it)')))
				)
				headers {
					header('Content-Type': value(
							producer(regex('application/vnd.fraud.v1.json.*')),
							consumer('application/vnd.fraud.v1+json'))
					)
				}
			}

}
