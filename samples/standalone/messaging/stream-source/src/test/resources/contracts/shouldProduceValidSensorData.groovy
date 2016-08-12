package contracts

org.springframework.cloud.contract.spec.Contract.make {
	// Human readable description
	description 'Should produce valid sensor data'
	// Label by means of which the output message can be triggered
	label 'sensor1'
	// input to the contract
	input {
		// the contract will be triggered by a method
		triggeredBy('createSensorData()')
	}
	// output message of the contract
	outputMessage {
		// destination to which the output message will be sent
		sentTo 'sensor-data'
		headers {
			header('contentType': 'application/json')
		}
		// the body of the output message
		body ([
				id: $(consumer(9), producer(regex("[0-9]+"))),
				temperature: "123.45"
		])
	}
}
