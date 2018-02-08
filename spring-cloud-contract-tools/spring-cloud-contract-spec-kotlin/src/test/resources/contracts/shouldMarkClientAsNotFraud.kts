package contracts

org.springframework.cloud.contract.spec.KotlinContract.make {
    request {
        method("PUT")
        url("/fraudcheck")
        body("""
						{
						"clientId":"${value(consumer(regex("[0-9]{10}")), producer("1234567890"))}",
						"loanAmount":123.123
						}
					"""
        )
        headers {
            contentType("application/vnd.fraud.v1+json")
        }

    }
    response {
        status(200)
        body(mapOf(
                "fraudCheckStatus" to "OK",
                "rejectionReason" to listOf(consumer(null), producer(execute("assertThatRejectionReasonIsNull($this)")))
        ))
        headers {
            contentType("application/vnd.fraud.v1+json")
        }
    }
}