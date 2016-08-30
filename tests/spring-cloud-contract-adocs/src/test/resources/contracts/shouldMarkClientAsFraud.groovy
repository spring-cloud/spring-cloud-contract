package contracts

org.springframework.cloud.contract.spec.Contract.make {
			description('''
			The following contract describes a situation where the user should NOT be accepted in our system.
			It satisfies the ticket number ABC-1234. For further information please contact the Product Owners
			John Doe and Jane Doe.

			Given:
				a user who wants to borrow a lot of money
			When:
				he applies for a loan
			Then:
				the loan should not be granted
			And:
				the user should marked as fraud
			''')
			request {
				method 'PUT'
				url '/fraudcheck'
				body([
					clientId: value(consumer(regex('[0-9]{10}'))),
					loanAmount: 99999
					])
				headers {
					header('Content-Type', 'application/vnd.fraud.v1+json')
				}
			}
			response {
				status 200
				body([
					fraudCheckStatus: "FRAUD",
					rejectionReason: "Amount too high"
				])
				headers {
					 header('Content-Type': value(
							 producer(regex('application/vnd.fraud.v1.json.*')),
							 consumer('application/vnd.fraud.v1+json'))
					 )
				}
			}
}