package io.codearte.accurest.dsl.internal

import groovy.transform.CompileStatic;
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString;

@ToString(includePackage = false, includeFields = true, includeNames = true)
@EqualsAndHashCode(includeFields = true)
@CompileStatic
class UrlPath extends Url {

	UrlPath(String path) {
		super(path)
	}

	UrlPath(DslProperty path) {
		super(path)
	}

}
