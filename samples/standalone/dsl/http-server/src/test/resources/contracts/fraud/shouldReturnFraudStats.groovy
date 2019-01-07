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
						count: $(regex("[2-9][0-9][0-9]").asInteger())
				])
				headers {
					contentType("application/json")
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
					contentType("application/json")
				}
			}
		}
]