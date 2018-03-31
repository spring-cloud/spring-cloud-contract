package contracts

org.springframework.cloud.contract.spec.Contract.make {
	request {
		method 'POST'
		url '/'
		body([
			   someInteger: $(c(anyInteger()), p(1234567890)),
			   someDecimal: $(c(anyDouble()), p(123.123)),
			   someHex: $(c(anyHex()), p('DEADC0DE')),
			   someAlphaNumeric: $(c(anyAlphaNumeric()), p('Some alpha numeric string with 1234567890')),
			   someUUID: $(c(anyUuid()), p('00000000-0000-0000-0000-000000000000')),
			   someDate: $(c(anyDate()), p('2018-03-26')),
			   someTime: $(c(anyTime()), p('13:37:00')),
			   someDateTime: $(c(anyDateTime()), p('2018-03-26 13:37:00')),
			   someBoolean: $(c(anyBoolean()), p('true')),
			   someRegex: $(c(regex('[0-9]{10}')), p(1234567890))
		])
		headers {
			contentType('application/json')
		}
	}
	response {
		status OK()
		body([
				someInteger: $(c(1234567890), p(anyInteger())),
				someDecimal: $(c(123.123), p(anyDouble())),
				someHex: $(c('DEADC0DE'), p(anyHex())),
				someAlphaNumeric: $(c('Some alpha numeric string with 1234567890'), p(anyAlphaNumeric())),
				someUUID: $(c('00000000-0000-0000-0000-000000000000'), p(anyUuid())),
				someDate: $(c('2018-03-26'), p(anyDate())),
				someTime: $(c('13:37:00'), p(anyTime())),
				someDateTime: $(c('2018-03-26 13:37:00'), p(anyDateTime())),
				someBoolean: $(c('true'), p(anyBoolean())),
				someRegex: $(c(1234567890), p(regex('[0-9]{10}')))
		])
		headers {
			contentType('application/json')
		}
	}
}