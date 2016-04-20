io.codearte.accurest.dsl.GroovyDsl.make {
	label 'some_label'
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