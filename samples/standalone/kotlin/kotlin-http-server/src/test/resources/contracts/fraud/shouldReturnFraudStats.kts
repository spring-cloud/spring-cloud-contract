package contracts

import org.springframework.cloud.contract.spec.contract

arrayOf(
		contract {
			request {
				name ("should count all frauds")
				method (GET())
				url ("/frauds")
			}
			response {
				status (200)
				body(
						"count" to 200
				)
				headers {
					contentType("application/json")
				}
			}
		},
		contract {
			request {
				method (GET())
				url ("/drunks")
			}
			response {
				status (200)
				body(
						"count" to 100
				)
				headers {
					contentType("application/json")
				}
			}
		}
)