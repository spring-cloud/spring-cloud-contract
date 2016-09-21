org.springframework.cloud.contract.spec.Contract.make {
	label 'delete_book'
	input {
		messageFrom('delete')
		messageBody([
				bookName: 'foo'
		])
		messageHeaders {
			header('sample', 'header')
		}
		assertThat('bookWasDeleted()')
	}
}