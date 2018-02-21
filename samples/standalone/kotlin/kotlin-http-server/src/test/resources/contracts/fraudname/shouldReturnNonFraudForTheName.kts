package contracts.fraudname

import org.springframework.cloud.contract.spec.contract

contract {
	request {
		method( PUT())
		url ("/frauds/name")
		body(
			   "name" to anyAlphaUnicode()
		)
		headers {
			contentType("application/json")
		}
	}
	response {
		status( 200)
		body(
				"result" to  "Don't worry ${fromRequest().body("$.name")} you're not a fraud"
		)
		headers {
			header(contentType(), "${fromRequest().header(contentType())};charset=UTF-8")
		}
	}
}