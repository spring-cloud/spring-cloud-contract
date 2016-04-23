package io.codearte.accurest.stubrunner.messaging.stream

import com.fasterxml.jackson.annotation.JsonCreator
import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode

@CompileStatic
@EqualsAndHashCode
class BookReturned implements Serializable {
	final String bookName

	@JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
	BookReturned(String bookName) {
		this.bookName = bookName
	}
}
