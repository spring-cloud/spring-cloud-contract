package io.codearte.accurest.samples.messaging

import com.fasterxml.jackson.annotation.JsonCreator
import groovy.transform.CompileStatic

@CompileStatic
class BookDeleted {
	final String bookName

	@JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
	BookDeleted(String bookName) {
		this.bookName = bookName
	}
}
