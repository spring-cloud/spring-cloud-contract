package contracts

import org.springframework.cloud.contract.spec.contract

contract {
    request {
        method("PUT")
        url("/fraudcheck")
        body("""
						{
						"clientId":"${dynamic(consumer = regex("[0-9]{10}"), producer = "1234567890")}",
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
        body(
                "fraudCheckStatus" to "OK",
                "rejectionReason" to listOf(dynamic(producer = execute("assertThatRejectionReasonIsNull(\$it)")))
        )
        headers {
            contentType("application/vnd.fraud.v1+json")
        }
    }
}