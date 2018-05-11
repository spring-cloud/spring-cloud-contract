org.springframework.cloud.contract.spec.Contract.make {
	description 'issue #650'
	label 'trigger'
	input {
		triggeredBy('createNewPerson()')
	}
	outputMessage {
		sentTo 'personEventsTopic'
		headers {
			[
					header('contentType': 'application/json'),
					header('type': 'person'),
					header('eventType': 'PersonChangedEvent'),
					header('customerId': $(producer(regex(uuid()))))
			]
		}
		body([
				"type"      : 'CREATED',
				"personId"  : $(producer(regex(uuid())), consumer('0fd552ba-8043-42da-ab97-4fc77e1057c9')),
				"userId"    : $(producer(optional(regex(uuid()))), consumer('f043ccf1-0b72-423b-ad32-4ef123718897')),
				"firstName" : $(regex(nonEmpty())),
				"middleName": $(optional(regex(nonEmpty()))),
				"lastName"  : $(regex(nonEmpty())),
				"version"   : $(producer(regex(number())), consumer(0l))
		])
	}
}