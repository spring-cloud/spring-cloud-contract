package io.codearte.accurest.samples.spring

import com.fasterxml.jackson.annotation.JsonCreator
import groovy.transform.CompileStatic

@CompileStatic
class BookDeleted implements Serializable {
	final String bookName

	@JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
	BookDeleted(String bookName) {
		this.bookName = bookName
	}
}
