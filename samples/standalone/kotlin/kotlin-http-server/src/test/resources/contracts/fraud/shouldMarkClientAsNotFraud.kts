package contracts

import org.springframework.cloud.contract.spec.contract

contract {
				request {
					method ("PUT")
					url ("/fraudcheck")
					body("""
						{
						"client.id":"${dynamic(consumer = regex("[0-9]{10}"), producer = "1234567890")}",
						"loanAmount":123.123
						}
					"""
					)
					headers {
						contentType("application/json")
					}

				}
			response {
				status (200)
				body(
						"fraudCheckStatus" to  "OK",
						"rejection.reason" to dynamic(producer = "execute('assertThatRejectionReasonIsNull(\$it)')")
				)
				headers {
					contentType("application/json")
				}
			}

}
