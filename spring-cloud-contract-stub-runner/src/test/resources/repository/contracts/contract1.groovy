org.springframework.cloud.contract.spec.Contract.make {
	request {
		method """PUT"""
		url """/fraudcheck"""
		body("""
					{
					"clientPesel":"${value(consumer(regex('[0-9]{10}')), producer('1234567890'))}",
					"loanAmount":99999}
				"""
		)
		headers {
			contentType("application/vnd.fraud.v1+json")
		}

	}
	response {
		status OK()
		body( """{
	"fraudCheckStatus": "${value(c('FRAUD'), p(regex('[A-Z]{5}')))}",
	"rejectionReason": "Amount too high"
}""")
		headers {
			contentType("application/vnd.fraud.v1+json")
		}
	}

}