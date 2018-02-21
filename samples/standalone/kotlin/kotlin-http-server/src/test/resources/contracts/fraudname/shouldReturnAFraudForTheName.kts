package contracts.fraudname

import org.springframework.cloud.contract.spec.contract

contract {
	// highest priority
	priority(1)
	request {
		method (PUT())
		url ("/frauds/name")
		body(
			   "name" to "fraud"
		)
		headers {
			contentType("application/json")
		}
	}
	response {
		status (200)
		body(
				"result" to "Sorry ${fromRequest().body("$.name")} but you're a fraud"
		)
		headers {
			header(contentType(), "${fromRequest().header(contentType())};charset=UTF-8")
		}
	}
}