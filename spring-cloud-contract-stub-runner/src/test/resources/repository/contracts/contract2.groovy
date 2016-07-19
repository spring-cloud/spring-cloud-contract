org.springframework.cloud.contract.spec.Contract.make  {
	// Human readable description
	description 'Sends an order message'
	// Label by means of which the output message can be triggered
	label 'send_order'
	// input to the contract
	input {
		// the contract will be triggered by a method
		triggeredBy('orderTrigger()')
	}
	// output message of the contract
	outputMessage {
		// destination to which the output message will be sent
		sentTo('orders')
		// any headers for the output message
		headers {
			header('contentType': 'application/json')
		}
		// the body of the output message
		body(
				orderId: value(
						consumer('40058c70-891c-4176-a033-f70bad0c5f77'),
						producer(regex('([0-9|a-f]*-*)*'))),
				description: "This is the order description"
		)
	}
}