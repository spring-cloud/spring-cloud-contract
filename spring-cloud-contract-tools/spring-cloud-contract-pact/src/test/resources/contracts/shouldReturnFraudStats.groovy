package contracts

import org.springframework.cloud.contract.spec.Contract

[
		Contract.make {
			request {
				name "should count all frauds"
				method GET()
				url '/frauds'
			}
			response {
				status OK()
				body([
						count: 200
				])
				headers {
					contentType("application/vnd.fraud.v1+json")
				}
			}
		},
		Contract.make {
			request {
				method GET()
				url '/drunks'
			}
			response {
				status OK()
				body([
						count: 100
				])
				headers {
					contentType("application/vnd.fraud.v1+json")
				}
			}
		}
]