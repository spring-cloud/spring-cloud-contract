package io.codearte.accurest.builder

import com.fasterxml.jackson.annotation.JsonCreator
import groovy.transform.CompileStatic

@CompileStatic
class BookReturned implements Serializable {
	final String bookName

	@JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
	BookReturned(String bookName) {
		this.bookName = bookName
	}
}
