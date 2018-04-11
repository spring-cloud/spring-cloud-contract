import org.springframework.cloud.contract.spec.Contract

Contract.make {
	request {
		method 'POST'
		url '/tests'
		multipart(
				[
						file1: named(
								name: value(consumer(regex(nonEmpty())), producer('filename1')),
								content: value(consumer(regex(nonEmpty())), producer('content1'))),
						file2: named(
								name: value(consumer(regex(nonEmpty())), producer('filename1')),
								content: value(c(regex(nonEmpty())), producer('content2'))),
						test : named(
								name: value(consumer(regex(nonEmpty())), producer('filename1')),
								content: value(c(regex(nonEmpty())), producer(file("test.json"))),
								contentType: "application/json")
				]
		)

		headers {
			contentType('multipart/form-data')
		}
	}
	response {
		status 200
		body([
				status: 'ok'
		])
		headers {
			contentType('application/json')
		}
	}
}
