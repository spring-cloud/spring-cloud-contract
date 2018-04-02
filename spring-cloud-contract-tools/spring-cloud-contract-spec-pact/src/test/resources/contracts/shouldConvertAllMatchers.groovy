package contracts

org.springframework.cloud.contract.spec.Contract.make {
	request {
		method 'POST'
		url '/'
		body([
			   someInteger: 1234567890,
			   someDecimal: 123.123,
			   someHex: 'DEADC0DE',
			   someAlphaNumeric: 'Some alpha numeric string with 1234567890',
			   someUUID: '00000000-0000-0000-0000-000000000000',
			   someDate: '2018-03-26',
			   someTime: '13:37:00',
			   someDateTime: '2018-03-26 13:37:00',
			   someBoolean: 'true',
			   someNullValue: null
		])
		headers {
			contentType('application/json')
			header("Some-Header", $(c(regex('[a-zA-Z]{9}')), p('someValue')))
			header("Header-Without-Matcher", 'someValue')
		}
		bodyMatchers {
			jsonPath('$.someInteger', byRegex(anInteger()))
			jsonPath('$.someDecimal', byRegex(aDouble()))
			jsonPath('$.someHex', byRegex('[a-fA-F0-9]+'))
			jsonPath('$.someAlphaNumeric', byRegex(alphaNumeric()))
			jsonPath('$.someUUID', byRegex(uuid()))
			jsonPath('$.someDate', byDate())
			jsonPath('$.someTime', byTime())
			jsonPath('$.someDateTime', byTimestamp())
			jsonPath('$.someBoolean', byRegex(anyBoolean()))
			jsonPath('$.someNullValue', byNull())
		}
	}
	response {
		status OK()
		body([
				someInteger: 1234567890,
				someDecimal: 123.123,
				someHex: 'DEADC0DE',
				someAlphaNumeric: 'Some alpha numeric string with 1234567890',
				someUUID: '00000000-0000-0000-0000-000000000000',
				someDate: '2018-03-26',
				someTime: '13:37:00',
				someDateTime: '2018-03-26 13:37:00',
				someBoolean: 'true',
				someRegex: 1234567890
		])
		headers {
			contentType('application/json')
			header("Some-Header", $(c('someValue'), p(regex('[a-zA-Z]{9}'))))
			header("Header-Without-Matcher", 'someValue')
		}
		bodyMatchers {
			jsonPath('$.someInteger', byRegex(anInteger()))
			jsonPath('$.someDecimal', byRegex(aDouble()))
			jsonPath('$.someHex', byRegex('[a-fA-F0-9]+'))
			jsonPath('$.someAlphaNumeric', byRegex(alphaNumeric()))
			jsonPath('$.someUUID', byRegex(uuid()))
			jsonPath('$.someDate', byDate())
			jsonPath('$.someTime', byTime())
			jsonPath('$.someDateTime', byTimestamp())
			jsonPath('$.someBoolean', byRegex(anyBoolean()))
			jsonPath('$.someNullValue', byNull())
		}
	}
}