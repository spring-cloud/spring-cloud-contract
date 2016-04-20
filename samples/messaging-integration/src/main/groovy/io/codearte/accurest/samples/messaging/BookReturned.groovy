package io.codearte.accurest.samples.messaging

import com.fasterxml.jackson.annotation.JsonCreator
import groovy.transform.CompileStatic

@CompileStatic
class BookReturned {
	final String bookName

	@JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
	BookReturned(String bookName) {
		this.bookName = bookName
	}
}
