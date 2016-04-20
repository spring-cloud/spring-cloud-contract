package io.codearte.accurest.samples.spring

import com.fasterxml.jackson.annotation.JsonCreator
import groovy.transform.CompileStatic

@CompileStatic
class BookReturned implements Serializable {
	String bookName

	@JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
	BookReturned(String bookName) {
		this.bookName = bookName
	}
}
