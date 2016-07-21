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
			header("""Content-Type""", """application/vnd.fraud.v1+json""")
		}

	}
	response {
		status 200
		body( """{
	"fraudCheckStatus": "${value(consumer('FRAUD'), producer(regex('[A-Z]{5}')))}",
	"rejectionReason": "Amount too high"
}""")
		headers {
			header('Content-Type': value(producer(regex('application/vnd.fraud.v1.json.*')), consumer('application/vnd.fraud.v1+json')))
		}
	}

}